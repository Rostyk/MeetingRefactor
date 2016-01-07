package com.exchange.ross.exchangeapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.exchange.ross.exchangeapp.Utils.DateUtils;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.Utils.ApplicationContextProvider;
import com.exchange.ross.exchangeapp.Utils.EventsManager;
import com.exchange.ross.exchangeapp.db.DatabaseManager;

import java.util.ArrayList;
import java.util.Collections;

public class EventDAO implements BaseDAO<Event>{
	private String sectionName;
	private String sectionNameInQuotes;
	private SQLiteDatabase database;
	private String DATABASE_TABLE = "Events";

	public EventDAO() {
        Context context = ApplicationContextProvider.getContext();

        if(context == null) {
            Log.v("Error", "Context invalid");
        }
	}
	

	public Event save(Event event) {
        initDB();

        String id = event.getId();
        ContentValues initialValues = new ContentValues();
        initialValues.put("subject", event.getSubject());
        initialValues.put("start_date", event.getStartDate());
        initialValues.put("end_date", event.getEndDate());
        initialValues.put("body", event.getBody());
        initialValues.put("account", event.getAccountName());
        initialValues.put("mute", event.getMute());
        initialValues.put("_id", id);
        initialValues.put("modified", event.getModified());
        initialValues.put("location", event.getLocation());
        initialValues.put("required_attendees", event.getRequiredAttendees());
        initialValues.put("optional_attendees", event.getOptionalAttendees());
        initialValues.put("calendarName", event.getCalendarName());
        initialValues.put("all_day", event.getAllDay());
        initialValues.put("busy", event.getBusy());
        long rows = database.insert(DATABASE_TABLE, null, initialValues);
        if(rows == -1) {
            Log.v("Error", "Row not inserted");
        }
        else {
            //EventsManager.sharedManager().setCachedEvents(null);
        }

        closeDB();
        return event;
    }

    public void insertEvents(ArrayList<Event> events) {
        initDB();
        database.beginTransaction();
        try {
            for (Event event : events) {
               save(event);
            }
            database.setTransactionSuccessful();

        } catch (Exception e) {
            Log.v("EX", "Error: " + e.getMessage());
        } finally {
            database.endTransaction();
        }

        closeDB();
    }

    public void sync(ArrayList<Event> itemsToInsert, ArrayList<Event> itemsToUpdate, ArrayList<Event> itemsToRemove) {
        initDB();
        database.beginTransaction();
        try {
            for (Event event : itemsToInsert) {
                save(event);
            }
            for (Event event : itemsToUpdate) {
                update(event);
            }

            for (Event event : itemsToRemove) {
                delete(event);
            }
            database.setTransactionSuccessful();

        } catch (Exception e) {
            Log.v("EX", "Error: " + e.getMessage());
        } finally {
            database.endTransaction();
        }

        closeDB();
    }

    public void delete(Event event) {
        initDB();
        database.delete(DATABASE_TABLE, "_id=" + "'" + event.getId() + "'", null);
        closeDB();
    }

    public Event update(Event event) {
        initDB();
        ContentValues values = new ContentValues();
        values.put("subject", event.getSubject());
        values.put("start_date", event.getStartDate());
        values.put("end_date", event.getEndDate());
        values.put("body", event.getBody());
        values.put("account", event.getAccountName());
        values.put("mute", event.getMute());
        values.put("modified", event.getModified());
        values.put("location", event.getLocation());
        values.put("required_attendees", event.getRequiredAttendees());
        values.put("optional_attendees", event.getOptionalAttendees());
        values.put("calendarName", event.getCalendarName());
        values.put("all_day", event.getAllDay());
        values.put("busy", event.getBusy());
        values.put("_id", event.getId());
        // updating row
        long res = database.update(DATABASE_TABLE, values, "_id=" + "'" + event.getId() + "'", null);
        if(res != -1) {
           // EventsManager.sharedManager().setCachedEvents(null);
        }
        closeDB();
        return event;
    }

    public Event getEventById(String id) {
        initDB();
        Event lastEvent = null;
        Cursor cursor = database.rawQuery("select * from " + DATABASE_TABLE + "  where _id = '" + id + "'", null);
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                Event event = getEventFromCursor(cursor);
                lastEvent = event;
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        return lastEvent;
    }

    public Iterable<Event> getAll() {
        initDB();

        //ArrayList<Event> cached = EventsManager.sharedManager().getCachedEvents();
        //if(cached != null) {
           //return cached;
        //}

        Cursor cursor = database.rawQuery("select * from " + DATABASE_TABLE, null);
        ArrayList<Event> allEvents = new ArrayList<Event>();
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                Event event = getEventFromCursor(cursor);
                allEvents.add(event);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        //Collections.sort(allEvents);

        //EventsManager.sharedManager().setCachedEvents(allEvents);
        return allEvents;

    }

    public Iterable<Event> getAll(int startDate, int endDate) {
        initDB();

        String start = DateUtils.dateSinceTodayWithString(startDate-1);
        String end = DateUtils.dateSinceTodayWithString(endDate-1);

        //ArrayList<Event> cached = EventsManager.sharedManager().getCachedEvents();
        //if(cached != null) {
        //return cached;
        //}

        Cursor cursor = database.rawQuery("select * from " + DATABASE_TABLE + " where start_date like '" + start + "%' OR start_date like '" + end + "%'", null);
        ArrayList<Event> allEvents = new ArrayList<Event>();
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                Event event = getEventFromCursor(cursor);
                allEvents.add(event);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        //Collections.sort(allEvents);

        //EventsManager.sharedManager().setCachedEvents(allEvents);
        return allEvents;

    }

    public Iterable<Event> getAll(String accountName) {
        initDB();

        //ArrayList<Event> cached = EventsManager.sharedManager().getCachedEvents();
        //if(cached != null) {
        //return cached;
        //}

        Cursor cursor = database.rawQuery("select * from " + DATABASE_TABLE + " WHERE account = '" + accountName + "'", null);
        ArrayList<Event> allEvents = new ArrayList<Event>();
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                Event event = getEventFromCursor(cursor);
                allEvents.add(event);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        //Collections.sort(allEvents);

        //EventsManager.sharedManager().setCachedEvents(allEvents);
        return allEvents;
    }

    private Event getEventFromCursor(Cursor cursor) {
        String subject = cursor.getString(cursor
                .getColumnIndex("subject"));

        String startDate = cursor.getString(cursor
                .getColumnIndex("start_date"));

        String endDate = cursor.getString(cursor
                .getColumnIndex("end_date"));

        String body = cursor.getString(cursor
                .getColumnIndex("body"));

        String accountName = cursor.getString(cursor
                .getColumnIndex("account"));

        Boolean mute = cursor.getInt(cursor
                .getColumnIndex("mute")) > 0;

        Boolean isAllDay = cursor.getInt(cursor
                .getColumnIndex("all_day")) > 0;

        Boolean busy = cursor.getInt(cursor
                .getColumnIndex("busy")) > 0;

        String modified = cursor.getString(cursor
                .getColumnIndex("modified"));

        String requiredAttendees = cursor.getString(cursor
                .getColumnIndex("required_attendees"));

        String optionalAttendees = cursor.getString(cursor
                .getColumnIndex("optional_attendees"));

        String calendarName = cursor.getString(cursor
                .getColumnIndex("calendarName"));


        String location = cursor.getString(cursor.getColumnIndex("location"));
        String id = cursor.getString(cursor
                .getColumnIndex("_id"));

        Event event = new Event(id, subject, startDate, endDate, body, accountName);
        event.setMute(mute);
        event.setAllDay(isAllDay);
        event.setBusy(busy);
        event.setModified(modified);
        event.setLocation(location);
        event.setRequiredAttendees(requiredAttendees);
        event.setOptionalAttendees(optionalAttendees);
        event.setCalendarName(calendarName);
        event.setId(id);
        return event;
    }
    private void initDB() {
         database = DatabaseManager.getInstance().openDatabase();
    }
    private void closeDB() {
         DatabaseManager.getInstance().closeDatabase();
    }
}

