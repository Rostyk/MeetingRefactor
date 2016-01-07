package com.exchange.ross.exchangeapp.db;

import android.os.AsyncTask;

import com.exchange.ross.exchangeapp.APIs.operations.OperationCompleted;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.db.dao.EventDAO;
import com.exchange.ross.exchangeapp.db.dao.async.Fetchtask;

import java.util.ArrayList;


public class EventsProxy {
	private static EventsProxy instance;
	private EventDAO eventDAO;

    public static synchronized EventsProxy sharedProxy() {
    if (instance == null)
            instance = new EventsProxy();
        return instance;
    }
    
    private EventsProxy() {
    	if(eventDAO == null) {
            eventDAO = new EventDAO();
    	}
    }

    /*
    public void getAllEventsInBackground(OperationCompleted completed) {
        Fetchtask operation = new Fetchtask(completed, -1);
        operation.execute(Integer.valueOf("12"));
    }*/

    public void getAllEventsInBackground(OperationCompleted completed, int daySinceToday) {
        Fetchtask operation = new Fetchtask(completed, daySinceToday);
        operation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Integer.valueOf("12"));
    }

    public ArrayList<Event> getAllEvents () {
        return (ArrayList<Event>)eventDAO.getAll();
    }


    public ArrayList<Event> getAllEvents(int daySinceNow) {
        ArrayList<Event> allEvents = (ArrayList<Event>)eventDAO.getAll();
        ArrayList<Event> todayEvents = EventsManager.sharedManager().eventsForDaySinceNow(daySinceNow, allEvents);
        return todayEvents;
    }

    public ArrayList<Event> getAllEvents(int startRange, int endRange) {
        ArrayList<Event> allEvents = (ArrayList<Event>)eventDAO.getAll(startRange, endRange);
        //ArrayList<Event> todayEvents = EventsManager.sharedManager().eventsForDaySinceNow(startRange, endRange, allEvents);
        return allEvents;
    }

    public ArrayList<ArrayList<Event>> getSyncEvents(ArrayList<Event> events, String accountName) {
        ArrayList<Event> alreadyStoredEvents = (ArrayList<Event>)eventDAO.getAll(accountName);
        ArrayList<Event> uniqueEvents = new ArrayList<Event>();
        ArrayList<Event> eventsToUpdate = new ArrayList<Event>();
        ArrayList<Event> eventsToRemove = new ArrayList<Event>();

        // get new events
        for(Event newEvent : events) {
            Boolean matches = false;
            Boolean shouldUpdate = false;

            for (Event storedEvent : alreadyStoredEvents) {
                if(newEvent.equals(storedEvent)) {
                    matches = true;
                    if(!newEvent.getModified().equalsIgnoreCase(storedEvent.getModified())) {
                        shouldUpdate = true;
                    }
                }
            }

            if(!matches) {
                uniqueEvents.add(newEvent);
            }

            if(shouldUpdate) {
                eventsToUpdate.add(newEvent);
            }
        }
        eventsToRemove = alreadyStoredEvents;
        for(Event newEvent : events) {
            eventsToRemove.remove(newEvent);
        }

        ArrayList<ArrayList<Event>> syncItems = new ArrayList<ArrayList<Event>>();
        syncItems.add(uniqueEvents);
        syncItems.add(eventsToUpdate);
        syncItems.add(eventsToRemove);

        return syncItems;
    }

    public void sync(ArrayList<Event> itemsToInsert, ArrayList<Event> itemsToUpdate, ArrayList<Event> itemsToRemove) {
       eventDAO.sync(itemsToInsert, itemsToUpdate, itemsToRemove);
    }

    public void addEvent(Event event) {
    	eventDAO.save(event);
    }

    public void insertEvents(ArrayList<Event> events) {
        eventDAO.insertEvents(events);
    }

    public void updateEvent(Event event) {
        eventDAO.update(event);
    }
    
    public void removeEvent(Event event) {
    	eventDAO.delete(event);
    }

    public Event getEventBuId(String id) {
        return eventDAO.getEventById(id);
    }

    public EventDAO getEventDAO() {
        return eventDAO;
    }
}
