package com.exchange.ross.exchangeapp.activity;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.exchange.ross.exchangeapp.APIs.GoogleWebService;
import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.APIs.operations.SyncEventCompleted;
import com.exchange.ross.exchangeapp.R;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.Utils.GATracker;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.core.service.TimeService;
import com.exchange.ross.exchangeapp.db.AccountsProxy;
import com.exchange.ross.exchangeapp.db.EventsProxy;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;


import java.util.ArrayList;
import java.util.Collections;


public class AddNewAccountActivity extends ActionBarActivity implements View.OnClickListener {
    private Boolean isAddingExchange = false;
    private Boolean isAddingExtraAcount = false;
    private ProgressDialog progress = null;
    private GoogleWebService service = null;
    static final int GOOGLE_CHOOSE_ACCOUNT_ACTIVITY = 1;
    private GoogleAccountCredential credential;
    private final int exGetEventsOperation = 11;
    String accountName;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final com.google.api.client.json.JsonFactory jsonFactory = GsonFactory.getDefaultInstance();


    @Override
    protected void onResume() {
        super.onResume();

        if(isAddingExchange && isAddingExtraAcount) {
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_account);

        //Google analitics
        GATracker.tracker().setScreenName("Login").sendEvent("Model", "Show", "");

        checkIfAddingExtraAccount();
        setupView();
    }

    public void checkIfAddingExtraAccount(){
        Intent intent = getIntent();
        isAddingExtraAcount = intent.getBooleanExtra("AddingExtraAccount", false);
    }

    public void setupView() {
        TextView singInTextView = (TextView)findViewById(R.id.singInTextView);
        Typeface robotoFaceLight = Typeface.createFromAsset(getAssets(),"fonts/robotolight.ttf");
        singInTextView.setTypeface(robotoFaceLight);


        ImageButton exchangeButton = (ImageButton) findViewById(R.id.exchangeButton);
        exchangeButton.setOnClickListener(this);

        ImageButton googleButtonButton = (ImageButton) findViewById(R.id.googleButton);
        googleButtonButton.setOnClickListener(this);

        ImageButton office365Button = (ImageButton) findViewById(R.id.office365Button);
        office365Button.setOnClickListener(this);
    }

    public void onClick(View view) {
        if(view.getId() == R.id.exchangeButton || view.getId() == R.id.office365Button) {
            GATracker.tracker().setScreenName("Login").sendEvent("UX", "Exchange button click", "");

            isAddingExchange = true;
            Intent exchangeLoginIntent = new Intent(AddNewAccountActivity.this, ExchangeLoginActivity.class);
            exchangeLoginIntent.putExtra("AddingExtraAccount", isAddingExtraAcount);
            AddNewAccountActivity.this.startActivity(exchangeLoginIntent);
        }
        if(view.getId() == R.id.googleButton) {
            GATracker.tracker().setScreenName("Login").sendEvent("UX", "Google button click", "");

            service = null;
            googleLogin();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void googleLogin() {
        GATracker.tracker().setScreenName("Login").sendEvent("API", "Star Google Login", "");
        // Google Accounts
        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Collections.singleton(CalendarScopes.CALENDAR));
        startActivityForResult(credential.newChooseAccountIntent(), GOOGLE_CHOOSE_ACCOUNT_ACTIVITY);
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == GOOGLE_CHOOSE_ACCOUNT_ACTIVITY && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            if(AccountsProxy.sharedProxy().isUnique(accountName)) {
                GATracker.tracker().setScreenName("Login").sendEvent("API", "Start Sync Google Events", "");
                getEvents();
            }
            else {
                GATracker.tracker().setScreenName("Login").sendEvent("API", "Google Login Failed. Account already linked", "");
                showWarning(getString(R.string.account_already_linked));
            }
        }

        if(requestCode == GoogleWebService.GOOGLE_PERMISSION_CODE) {
            service.setPermissionPassed(true);
            getEvents();
        }
    }

    public void getEvents() {
        progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.google_calendar));
        progress.setMessage(getString(R.string.syncing_events));
        progress.show();

        if(service == null)
           service = new GoogleWebService("", accountName, "", "", getApplicationContext(), AddNewAccountActivity.this);

            service.getEvents(new OperationCompleted() {
                @Override
                public void onOperationCompleted(Object result, int id) {
                    if(id == exGetEventsOperation && result != null) {
                        if(isAddingExtraAcount) {
                            saveEvents((ArrayList<Event>) result);
                            saveAccount(service);
                            updateFragmentsUI();
                            finish();
                        }
                        else {
                            ArrayList<Event> events = (ArrayList<Event> )result;
                            saveEvents(events);
                            saveAccount(service);
                            progress.dismiss();
                            openEventsActivity();
                        }
                    }
                    else if(result == null) {
                        if(isAddingExtraAcount) {
                            removeAccount(service);
                            finish();
                        }
                        if(progress != null)
                           progress.dismiss();
                        showWarning(getString(R.string.cant_link_this_account));
                    }
                }
            }, exGetEventsOperation);
    }

    public void updateFragmentsUI() {
        Intent newEventsIntent = new Intent(TimeService.SYNC_NEW_EVENTS_BR);
        LocalBroadcastManager.getInstance(ApplicationContextProvider.getContext()).sendBroadcast(newEventsIntent);
    }

    public void saveAccount(GoogleWebService service) {
        AccountsProxy proxy = AccountsProxy.sharedProxy();
        proxy.addAccount(service);
    }

    public void removeAccount(GoogleWebService service) {
        AccountsProxy proxy = AccountsProxy.sharedProxy();
        proxy.removeAccount(service);
    }

    public void saveEvents(ArrayList<com.exchange.ross.exchangeapp.core.entities.Event> events) {
        EventsProxy proxy = EventsProxy.sharedProxy();
        proxy.insertEvents(events);
    }

    public void openEventsActivity() {
        Intent eventsActivityIntent = new Intent(AddNewAccountActivity.this, EventsActivity.class);
        AddNewAccountActivity.this.startActivity(eventsActivityIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progress != null)
           progress.dismiss();

        if(service != null)
           service.terminate();
    }

    public void onBackPressed() {
        if(isAddingExtraAcount) {
            finish();
        }
        else {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(i);
        }
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
