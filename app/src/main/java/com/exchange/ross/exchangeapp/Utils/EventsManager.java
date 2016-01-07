package com.exchange.ross.exchangeapp.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.exchange.ross.exchangeapp.APIs.operations.SyncEventCompleted;
import com.exchange.ross.exchangeapp.activity.EventsActivity;
import com.exchange.ross.exchangeapp.core.entities.Event;
import com.exchange.ross.exchangeapp.core.service.TimeService;
import com.exchange.ross.exchangeapp.db.DatabaseManager;
import com.exchange.ross.exchangeapp.db.EventsProxy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.exchange.ross.exchangeapp.R;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ross on 3/31/15.
 */
public class EventsManager {

    private ArrayList<String> displayedNotificationIDs = new ArrayList<String>();
    public static final String KILL_SERVICE = "com.ross.exchangeapp.kill_service";
    public static final String START_SERVICE = "com.ross.exchangeapp.start_service";
    public static final String FORCE_SYNC_EVENTS = "com.ross.exchange.force_sync";
    public static final String FORCE_SYNC_EVENTS_IN_SERVICE = "com.ross.exchange.force_sync_in_service";
    private Intent killServiceIntent = new Intent(KILL_SERVICE);
    private Intent restartServiceIntent = new Intent(START_SERVICE);
    private Intent forceSyncEventsIntent = new Intent(FORCE_SYNC_EVENTS);
    private SyncEventCompleted syncEventsCompleted;
    private Boolean listNeedsRefresh = false;
    private int index;
    private static EventsManager instance;
    private String selectedEventId;
    private ArrayList<Event> ongoingEvents = new ArrayList<Event>();
    private int volume;
    private Boolean muted = true;
    private ArrayList<Event> cachedEvents;

    public static synchronized EventsManager sharedManager() {
        if (instance == null)
            instance = new EventsManager();
        return instance;
    }

    private EventsManager() {
        registerReceiver();
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(TimeService.TIMER_BR);
        filter.addAction(TimeService.SYNC_NEW_EVENTS_BR);
        LocalBroadcastManager.getInstance(ApplicationContextProvider.getsContext()).registerReceiver(eventsSyncBroadcastReceiver, filter);
    }

    public ArrayList<Event> eventsForDaySinceNow(int daySinceToday, ArrayList<Event>events) {
        Date date = DateUtils.dateSinceToday(daySinceToday);

        ArrayList<Event> filteredEvents = new ArrayList<Event>();
        for(Event event : events) {
            if(isSameDay(date, event.getStartDateInDate())) {
                filteredEvents.add(event);
            }
        }
        sort(filteredEvents);
        return filteredEvents;
    }

    public ArrayList<Event> eventsForDaySinceNow(int startRange, int endRange, ArrayList<Event>events) {
        ArrayList<Event> filteredEvents = new ArrayList<Event>();
        for(int _start = startRange; _start<= endRange; _start++) {
            Date date = DateUtils.dateSinceToday(_start);
            for(Event event : events) {
                if (isSameDay(date, event.getStartDateInDate())) {
                    filteredEvents.add(event);
                }
            }
        }
        sort(filteredEvents);
        return filteredEvents;
    }

    public ArrayList<Event> eventsByDate(Date date, ArrayList<Event>events) {
        ArrayList<Event> filteredEvents = new ArrayList<Event>();
        for(Event event : events) {
            if(isSameDay(date, event.getStartDateInDate())) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    public void countOngoingEvents() {
        ArrayList<Event> events = EventsProxy.sharedProxy().getAllEvents(1, 2);

        Date now = new Date();
        ongoingEvents = new ArrayList<Event>();
        for (Event event : events) {
            Date startDate = event.getStartDateInDate();
            Date endDate = event.getEndDateInDate();
            if (isWithinRange(now, startDate, endDate)) {
                event.checkIfAllDayEvent();
                if(!event.getAllDay())
                    ongoingEvents.add(event);
            }
        }
        notifyStatus(ongoingEvents);

        Boolean needsMute = false;
        for(Event event : ongoingEvents) {
            if(event.getMute()) {
                needsMute = true;
            }
        }

        checkMuteState(needsMute);
    }

    private void checkMuteState(Boolean mute) {
        SoundManager soundManager = SoundManager.sharedManager();
        if(mute) {
            soundManager.mute(mute);
            muted = true;
        }
        else {
            if(muted) {
                soundManager.mute(mute);

                muted = false;
            }
        }
    }

    public void notifyStatus(ArrayList<Event> events) {
        NotificationManager notificationManager = (NotificationManager)ApplicationContextProvider.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //Remove ongoing meetings which passed already
        if(displayedNotificationIDs.size() > events.size()) {
            ArrayList<String> notificationsToCancel = new  ArrayList<String>();
            for(String id : displayedNotificationIDs) {
                Boolean matches = false;
                for (Event event : events) {
                    if(event.getId().equals(id))
                        matches = true;
                }
                if(!matches)
                    notificationsToCancel.add(id);
            }
            for(String id : notificationsToCancel) {
                notificationManager.cancel(id.hashCode());
            }
        }
        for(Event event : events) {
            int notificationId = Math.abs(event.getId().hashCode());
            index++;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationContextProvider.getContext());
            double minutesElapsed = getDateDiff(event.getStartDateInDate(), new Date(), TimeUnit.MINUTES);
            double duration = getDateDiff(event.getStartDateInDate(),event.getEndDateInDate(), TimeUnit.MINUTES);

            double progress = (minutesElapsed*1.0 / duration*1.0);
            int res = 0 + (int)(progress * 100.0);
            int drawableResourceId = ApplicationContextProvider.getsContext().getResources().getIdentifier("progress" + res + "", "drawable", ApplicationContextProvider.getsContext().getPackageName());

            // The PendingIntent will launch activity if the user selects this notification
            Intent intent = new Intent(ApplicationContextProvider.getContext(), EventsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("com.ross.exchange.eventID", notificationId);
            PendingIntent contentIntent = PendingIntent.getActivity(ApplicationContextProvider.getContext(), notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = builder.setContentIntent(contentIntent)
                    .setSmallIcon(drawableResourceId)
                    .setContentTitle(event.getSubject())
                    .setContentText(minutesElapsed + " " + ApplicationContextProvider.getContext().getResources().getString(R.string.of) + " " + duration)
                    .build();
            //notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(notificationId , notification);
            displayedNotificationIDs.clear();
            displayedNotificationIDs.add(event.getId());
        }
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }


    public String getSelectedEventId() {
        return selectedEventId;
    }

    public void setSelectedEventId(String selectedEventId) {
        this.selectedEventId = selectedEventId;
    }

    public ArrayList<Event> ongoingEvents() {
        return ongoingEvents;
    }

    boolean isWithinRange(Date testDate, Date startDate, Date endDate) {
        return !(testDate.before(startDate) || testDate.after(endDate));
    }

    boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(date1).equals(format.format(date2));
    }

    public void setCachedEvents(ArrayList<Event> cachedEvents) {
        if(this.cachedEvents != null) {
            this.cachedEvents.clear();
        }
        this.cachedEvents = cachedEvents;
    }

    public Boolean getListNeedsRefresh() {
        return listNeedsRefresh;
    }

    public void setListNeedsRefresh(Boolean listNeedsRefresh) {
        this.listNeedsRefresh = listNeedsRefresh;
    }

    public void unlinkAllAccounts() {
        DatabaseManager.unlink();
    }

    public void suspendSyncService() {
        LocalBroadcastManager.getInstance(ApplicationContextProvider.getContext()).sendBroadcast(killServiceIntent);
    }

    public void restartSyncService() {
        LocalBroadcastManager.getInstance(ApplicationContextProvider.getContext()).sendBroadcast(restartServiceIntent);
    }

    public void sync(String account, SyncEventCompleted completion) {
        syncEventsCompleted = completion;
        forceSyncEventsIntent.putExtra("Account", account);
        LocalBroadcastManager.getInstance(ApplicationContextProvider.getContext()).sendBroadcast(forceSyncEventsIntent);
    }

    public void syncCompleted(Boolean success) {
        if(syncEventsCompleted != null) {
            syncEventsCompleted.onSyncEventsCompleted(success);
            syncEventsCompleted = null;
        }
    }

    private BroadcastReceiver eventsSyncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TimeService.SYNC_NEW_EVENTS_BR)) {

            }
        }
    };

    public ArrayList<Event> getCachedEvents() {
        return cachedEvents;
    }

    public void sort(ArrayList<Event> events) {
        for(Event event : events) {
            event.checkIfAllDayEvent();
        }
        Collections.sort(events);
    }
}
