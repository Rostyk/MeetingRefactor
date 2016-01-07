package com.exchange.ross.exchangeapp.core.service;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.exchange.ross.exchangeapp.APIs.WebService;
import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.APIs.operations.SyncEventCompleted;
import com.exchange.ross.exchangeapp.ISync;
import com.exchange.ross.exchangeapp.IUpdateUIStart;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.Utils.Settings;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.db.AccountsProxy;
import com.exchange.ross.exchangeapp.db.DatabaseManager;
import com.exchange.ross.exchangeapp.db.EventsProxy;
import com.exchange.ross.exchangeapp.db.WHDatabaseHelper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeService extends Service {
    private IUpdateUIStart fragmentsUiUpdater;
    private int time = 0;
    private Boolean terminated = false;
    ArrayList<WebService> services;
    private ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();
    private final int exGetEventsOperation = 19;
    private static Timer syncTimer;
    private static Timer timer;
    private Context ctx;
    public static final String TIMER_BR = "com.ross.exchangeapp.timer";
    public static final String SYNC_NEW_EVENTS_BR = "com.ross.exchnageapp.new_events";
    Intent bi = new Intent(TIMER_BR);
    private SyncEventCompleted mListener;
    Intent newEventsIntent = new Intent(SYNC_NEW_EVENTS_BR);

    public void onCreate() {
        super.onCreate();
        ctx = this;
        startService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    /**
     * IAdd definition is below
     */
    private final ISync.Stub mBinder = new ISync.Stub() {
        @Override
        public void attachUIUpdate(IUpdateUIStart uiUpdater) {
            fragmentsUiUpdater = uiUpdater;
        }

        @Override
        public void sync(String accountName) {
            scheduleSyncTimer(1000);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startService() {
        scheduleSyncTimer(560000);
        scheduleOngoingTimer();
        Settings.sharedSettings().setContext(this);
    }

    public void scheduleSyncTimer(int time) {
        if(syncTimer != null)
           syncTimer.cancel();

        syncTimer = new Timer();
        syncTimer.schedule (new syncEventsTask(), time);
    }

    public void scheduleOngoingTimer() {
        if(timer != null)
            timer.cancel();
        timer = new Timer();
        timer.schedule(new checkEventsTask(), 30000);
    }


    private class checkEventsTask extends TimerTask {
        public void run() {
            checkEvents();
        }
    }

    private void checkEvents() {
        scheduleOngoingTimer();
        ApplicationContextProvider.setApplicationContext(getApplicationContext());
        DatabaseManager.initializeInstance(new WHDatabaseHelper(getApplicationContext()));

        EventsManager.sharedManager().countOngoingEvents();
        LocalBroadcastManager.getInstance(ApplicationContextProvider.getContext()).sendBroadcast(bi);
    }

    private class syncEventsTask extends TimerTask {
        public void run() {
            //null for all accounts
            syncEvents(null);
        }
    }

    private void syncEvents(String account) {
        scheduleSyncTimer(560000);
        ApplicationContextProvider.setApplicationContext(this);
        DatabaseManager.initializeInstance(new WHDatabaseHelper(this));

        services = AccountsProxy.sharedProxy().getAllAccounts(this);

        if(account != null) {
            services = filter(services, account);
        }

        if(services.size() > 0) {
            for (final WebService service : services) {
                service.getEvents(new OperationCompleted() {
                    @Override
                    public void onOperationCompleted(Object result, int id) {
                        String syncedAccount = service.getCredentials().getUser();
                        if(id == exGetEventsOperation && result != null) {
                            ArrayList<Event> events = (ArrayList<Event> )result;

                            //Save new events which are not in the database
                            ArrayList<ArrayList<Event>> syncEvents = EventsProxy.sharedProxy().getSyncEvents(events, syncedAccount);

                            ArrayList<Event> itemsToInsert = syncEvents.get(0);
                            ArrayList<Event> itemsToUpdate = syncEvents.get(1);
                            ArrayList<Event> itemsToRemove = syncEvents.get(2);

                            if(!terminated)
                               sync(syncedAccount, itemsToInsert, itemsToUpdate, itemsToRemove);
                        }
                        else {

                        }

                    }
                }, exGetEventsOperation);
            }
        }
    }

    public void sync(String accountName, ArrayList<Event> itemsToInsert, ArrayList<Event> itemsToUpdate, ArrayList<Event> itemsToRemove) {
        EventsProxy proxy = EventsProxy.sharedProxy();
        proxy.sync(itemsToInsert, itemsToUpdate, itemsToRemove);
        updateUI(accountName);
    }

    public void updateUI(final String accountName) {
        EventsManager.sharedManager().countOngoingEvents();
        try {
            if(fragmentsUiUpdater != null)
               fragmentsUiUpdater.updateUI();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<WebService> filter(ArrayList<WebService> services, String account) {
        ArrayList<WebService> filteredList = new ArrayList<WebService>();

        for (WebService s : services) {
            if (s.getCredentials().getUser().equals(account)) {
                filteredList.add(s);
            }
        }

        return filteredList;
    }

    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, TimeService.class)); // add this line
        terminated = true;
        timer.cancel();
        syncTimer.cancel();
        for(WebService service: services) {
            service.terminate();
        }
    }

}
