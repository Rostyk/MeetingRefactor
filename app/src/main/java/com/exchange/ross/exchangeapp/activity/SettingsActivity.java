package com.exchange.ross.exchangeapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.exchange.ross.exchangeapp.APIs.WebService;
import com.exchange.ross.exchangeapp.R;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.Utils.GATracker;
import com.exchange.ross.exchangeapp.Utils.PurchaseManager;
import com.exchange.ross.exchangeapp.Utils.Settings;
import com.exchange.ross.exchangeapp.Utils.Typefaces;
import com.exchange.ross.exchangeapp.Utils.billing.OnPurchased;
import com.exchange.ross.exchangeapp.db.AccountsProxy;

import java.util.ArrayList;

public class SettingsActivity extends ActionBarActivity {
    private ArrayList<RelativeLayout> accountViews = new ArrayList<RelativeLayout>();
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        activity = this;

        Typeface thin = Typefaces.get(getApplicationContext(), "robotothin");
        TextView settingsTopTextView = (TextView)findViewById(R.id.settingsTopTextView);
        settingsTopTextView.setTypeface(thin);

        ImageButton backButton = (ImageButton)findViewById(R.id.settingsBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupSwitchesHandlers();
        setupAccountsSettingsButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupLinkedAccountsSection();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    public void setupSwitchesHandlers() {
         Typeface light = Typefaces.get(getApplicationContext(), "robotolight");

         //Switch soundSwitch = (Switch)findViewById(R.id.soundSwitch);
         //soundSwitch.setTypeface(light);

         Switch vibrationSwitch = (Switch)findViewById(R.id.vibrationSwitch);
         vibrationSwitch.setTypeface(light);

         Switch statusBusySwitch = (Switch)findViewById(R.id.statusBusySwitch);
         statusBusySwitch.setTypeface(light);

         Switch ignoreAllDaySwitch = (Switch)findViewById(R.id.ignoreAllDaySwitch);
         ignoreAllDaySwitch.setTypeface(light);

         Switch timerSwitch = (Switch)findViewById(R.id.timerSwitch);
         timerSwitch.setTypeface(light);

         Switch listSwitch = (Switch)findViewById(R.id.listSwitch);
         listSwitch.setTypeface(light);

        final Settings settings = Settings.sharedSettings();


        vibrationSwitch.setChecked(settings.getVibration());
        vibrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVibration(isChecked);
                GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Vibration changed " + isChecked, "");
        }
        });

        statusBusySwitch.setChecked(settings.getSilentOnStatusBusy());
        statusBusySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setSilentOnStatusBusy(isChecked);
                GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Status busy changed " + isChecked, "");

            }
        });

        ignoreAllDaySwitch.setChecked(settings.getIgnoreAllDayEvent());
        ignoreAllDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setIgnoreAllDayEvent(isChecked);
                GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Ignore all day  changed " + isChecked, "");
            }
        });

        listSwitch.setChecked(settings.getListMeetingsForDay());
        listSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setListMeetingsForDay(isChecked);
                GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Show all meetings for the day changed " + isChecked, "");
            }
        });

        timerSwitch.setChecked(settings.getTimer());
        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setTimer(isChecked);
                GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Timer during meeting changed " + isChecked, "");
            }
        });

        TextView soundTextView = (TextView)findViewById(R.id.settingSoundTextView);
        soundTextView.setTypeface(light);
        TextView meetingTextView = (TextView)findViewById(R.id.settingMeetingTextView);
        meetingTextView.setTypeface(light);
        TextView notificationTextView = (TextView)findViewById(R.id.settingNotificationTextView);
        notificationTextView.setTypeface(light);

        TextView settingAddNewAccountTextView = (TextView)findViewById(R.id.settingAddNewAccountTextView);
        TextView settingRemoveAllAccountsTextView = (TextView)findViewById(R.id.settingRemoveAllAccountsTextView);
        settingAddNewAccountTextView.setTypeface(light);
        settingRemoveAllAccountsTextView.setTypeface(light);

        TextView settingAccountTextView = (TextView)findViewById(R.id.settingAccountsTextView);
        settingAccountTextView.setTypeface(light);

        TextView settingManageAccountsTextView = (TextView)findViewById(R.id.settingManageAccountsTextView);
        settingManageAccountsTextView.setTypeface(light);
        setupLinkedAccountsSection();
    }

    public void setupLinkedAccountsSection() {
        Context context = getApplicationContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ScrollView scrollView = (ScrollView)findViewById(R.id.settingsScrollView);

        LinearLayout containingLayout = (LinearLayout)scrollView.findViewById(R.id.containingView);
        LinearLayout settingsLinkedAccountsView = (LinearLayout)containingLayout.findViewById(R.id.settingsLinkedAccountsView);

        //remove already existing views
        for(RelativeLayout layout : accountViews) {
            settingsLinkedAccountsView.removeView(layout);
        }

        ArrayList<WebService> accounts =  AccountsProxy.sharedProxy().getAllAccounts(getApplicationContext());
        for(WebService account : accounts) {
            RelativeLayout accountLayout = (RelativeLayout) inflater.inflate(R.layout.account_view, null, false);
            settingsLinkedAccountsView.addView(accountLayout);
            accountViews.add(accountLayout);
            TextView accountTextView = (TextView)accountLayout.findViewById(R.id.settingsAccountTextView);
            accountTextView.setText(account.getCredentials().getUser());
        }
    }

    public void setupAccountsSettingsButton() {
        View linkAccountView = findViewById(R.id.settingsAddAccountView);
        linkAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PurchaseManager.sharedManager().getAlreadyOwned()) {
                    GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Link new account clicked. Purchased", "");
                    addNewAccount();
                }
                else {
                    GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Link new account clicked. Not Purchased", "");
                    PurchaseManager manager = PurchaseManager.sharedManager();
                    manager.buy(new OnPurchased() {
                        @Override
                        public void onPurchaseComplete(Boolean success) {
                            addNewAccount();
                        }
                    }, activity);
                }
            }
        });

        View unlinkAccountsView = findViewById(R.id.settingsRemoveAllAccountsView);
        unlinkAccountsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GATracker.tracker().setScreenName("Settings").sendEvent("UX", "Unlink all clicked", "");

                EventsManager.sharedManager().unlinkAllAccounts();

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public void addNewAccount() {
        Intent addNewAccountIntent = new Intent(SettingsActivity.this, AddNewAccountActivity.class);
        addNewAccountIntent.putExtra("AddingExtraAccount", true);
        startActivity(addNewAccountIntent);
    }

}

