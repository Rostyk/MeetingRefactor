package com.exchange.ross.exchangeapp.db.dao.async;

import android.os.AsyncTask;

import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.db.EventsProxy;

import java.util.ArrayList;

/**
 * Created by ross on 4/5/15.
 */
public class Fetchtask extends AsyncTask<Integer, Float, ArrayList<Event>> {
    private OperationCompleted listener;
    private int daySinceNow;

    public Fetchtask(OperationCompleted listener, int daySinceNow){
        this.listener = listener;
        this.daySinceNow = daySinceNow;
    }

    protected ArrayList<Event> doInBackground(Integer ...params) {
        ArrayList<Event> allEvents = new ArrayList<Event>();
        try{
            allEvents = (ArrayList<Event>) EventsProxy.sharedProxy().getAllEvents(daySinceNow, daySinceNow + 1);
            if(daySinceNow == -1) {
                //return all events
            }
            else {
                ArrayList<Event> todayEvents = EventsManager.sharedManager().eventsForDaySinceNow(daySinceNow, allEvents);
                allEvents = todayEvents;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return allEvents;
    }

    @Override
    protected void onPostExecute(ArrayList<Event> events) {
        super.onPostExecute(events);
        listener.onOperationCompleted(events, this.daySinceNow);
    }
}
