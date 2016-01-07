package com.exchange.ross.exchangeapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.exchange.ross.exchangeapp.APIs.ExchangeWebService;
import com.exchange.ross.exchangeapp.APIs.GoogleWebService;
import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.APIs.operations.SyncEventCompleted;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.Utils.GATracker;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.core.service.TimeService;
import com.exchange.ross.exchangeapp.db.AccountsProxy;
import com.exchange.ross.exchangeapp.db.EventsProxy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;
import java.util.List;

import com.exchange.ross.exchangeapp.R;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class ExchangeLoginActivity extends ActionBarActivity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private final int exGetEvetsOperation = 12;
    // UI references.
    private AutoCompleteTextView mURLView;
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mEmailLoginFormView;
    private SignInButton mPlusSignInButton;
    private View mSignOutButtons;
    private View mLoginFormView;
    private Boolean isAddingExtraAcount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_login);

        // Set up the login form.
        mURLView = (AutoCompleteTextView) findViewById(R.id.urlEditText);
        mUserView = (AutoCompleteTextView) findViewById(R.id.userEditText);

        mPasswordView = (EditText) findViewById(R.id.passwordEditText);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailLoginFormView = findViewById(R.id.email_login_form);
        mSignOutButtons = findViewById(R.id.plus_sign_out_buttons);

        checkIfAddingExtraAccount();
    }


    public void checkIfAddingExtraAccount(){
        Intent intent = getIntent();
        isAddingExtraAcount = intent.getBooleanExtra("AddingExtraAccount", false);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mURLView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String url = mURLView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(url)) {
            mURLView.setError(getString(R.string.error_field_required));
            focusView = mURLView;
            cancel = true;
        } else if (!isURLValid(url)) {
            mURLView.setError(getString(R.string.error_invalid_url));
            focusView = mURLView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            login();
        }
    }

    public void login() {
        GATracker.tracker().setScreenName("Login").sendEvent("API", "Start Sync Exchange Events", "");

        String userAndDomain = mUserView.getText().toString();
        String[] parts = userAndDomain.split("\\\\");

        if(parts.length == 2) {
            final String user = parts[1];

            if(!AccountsProxy.sharedProxy().isUnique(user)) {
                showWarning(getString(R.string.account_already_linked));
                return;
            }
            String domain = parts[0];
            String password = mPasswordView.getText().toString();

            final ExchangeWebService service = new ExchangeWebService(mURLView.getText().toString(), user, password, domain);
            service.getEvents(new OperationCompleted() {
                    @Override
                    public void onOperationCompleted(Object result, int id) {
                        if (id == exGetEvetsOperation && result != null) {
                            if (isAddingExtraAcount) {
                                saveAccount(service);
                                saveEvents((ArrayList<Event>) result);
                                updateFragmentsUI();
                                finish();
                            }
                            else {
                                ArrayList<Event> events = (ArrayList<Event>) result;
                                saveAccount(service);
                                saveEvents(events);
                                openEventsActivity();
                            }

                        }
                        else if (result == null) {
                            removeAccount(service);
                            finish();
                        }
                        showProgress(false);
                    }
                }, exGetEvetsOperation);
        }
    }

    public void updateFragmentsUI() {
        Intent newEventsIntent = new Intent(TimeService.SYNC_NEW_EVENTS_BR);
        LocalBroadcastManager.getInstance(ApplicationContextProvider.getContext()).sendBroadcast(newEventsIntent);
    }

    public void onOperationCompleted(ArrayList<Event> events, int id) {

    }

    private boolean isURLValid(String url) {
        //TODO: Replace this with your own logic
        return url.contains(".");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    protected void onCancelled() {
        showProgress(false);
    }

    public void saveAccount(ExchangeWebService service) {
        AccountsProxy proxy = AccountsProxy.sharedProxy();
        proxy.addAccount(service);
    }

    public void removeAccount(ExchangeWebService service) {
        AccountsProxy proxy = AccountsProxy.sharedProxy();
        proxy.removeAccount(service);
    }

    public void saveEvents(ArrayList<com.exchange.ross.exchangeapp.core.entities.Event> events) {
        EventsProxy proxy = EventsProxy.sharedProxy();
        proxy.insertEvents(events);
    }

    public void openEventsActivity() {
        Intent eventsActivityIntent = new Intent(ExchangeLoginActivity.this, EventsActivity.class);
        ExchangeLoginActivity.this.startActivity(eventsActivityIntent);
    }

    public void showWarning(String message) {
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}



