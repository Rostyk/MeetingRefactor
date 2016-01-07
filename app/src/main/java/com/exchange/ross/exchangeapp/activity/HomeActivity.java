package com.exchange.ross.exchangeapp.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.R;
import com.exchange.ross.exchangeapp.Utils.GATracker;
import com.exchange.ross.exchangeapp.Utils.Settings;
import com.exchange.ross.exchangeapp.db.AccountsProxy;
import com.exchange.ross.exchangeapp.db.DatabaseManager;
import com.exchange.ross.exchangeapp.db.WHDatabaseHelper;

public class HomeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApplicationContextProvider.setActivity(this);
        ApplicationContextProvider.setApplicationContext(getApplicationContext());
        Settings.setContext(getApplicationContext());
        DatabaseManager.initializeInstance(new WHDatabaseHelper(getApplicationContext()));
        setContentView(R.layout.activity_home);
        start();
    }

    public void start() {

        GATracker.tracker().sendEvent("App", "Launch", "");
        //no accounts linked
        if(AccountsProxy.sharedProxy().getAllAccounts(getApplicationContext()).size() == 0) {
            Intent addNewAccountIntent = new Intent(HomeActivity.this, AddNewAccountActivity.class);
            HomeActivity.this.startActivity(addNewAccountIntent);
        }
        else {
            Intent eventsActivityIntent = new Intent(HomeActivity.this, EventsActivity.class);
            HomeActivity.this.startActivity(eventsActivityIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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
}
