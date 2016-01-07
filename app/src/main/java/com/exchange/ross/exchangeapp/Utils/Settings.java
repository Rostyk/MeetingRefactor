package com.exchange.ross.exchangeapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by ross on 4/20/15.
 */
public class Settings {
    private static Settings instance;

    private static Boolean involvesEvensListReloadByChangingStatusBusy = false;
    private static Boolean involvesEvensListReloadByChangingIgnoreAllDayEvents = false;

    private static Boolean sound = false;
    private static String kSound = "sound";
    private static Boolean vibration  = false;
    private static String kVibration = "vibration";
    private static Boolean ignoreAllDayEvent = false;
    private static String kIgnoreAllDayEvent = "ignoreAllDayEvent";
    private static Boolean silentOnStatusBusy = false;
    private static String kSilentOnStatusBusy = "silentOnStatusBusy";
    private static Boolean listMeetingsForDay = false;
    private static String kListMeetingsForDay = "listMeetingsForDay";
    private static Boolean timer = false;
    private static String kTimer = "timer";

    private static Context context;
    private static SharedPreferences preferences;

    public static synchronized Settings sharedSettings() {
        if (instance == null)
            instance = new Settings();
        return instance;
    }

    public Boolean getSound() {
        return this.sound;
    }

    public static synchronized Boolean getVibration() {
        if(context != null) {
            Settings.setContext(context);
        }
        return vibration;
    }

    public static synchronized Boolean getIgnoreAllDayEvent() {
        if(context != null) {
            Settings.setContext(context);
        }
        return ignoreAllDayEvent;
    }

    public static synchronized Boolean getSilentOnStatusBusy() {
        if(context != null) {
            Settings.setContext(context);
        }
        return silentOnStatusBusy;
    }

    public static synchronized Boolean getListMeetingsForDay() {
        if(context != null) {
            Settings.setContext(context);
        }
        return listMeetingsForDay;
    }

    public Boolean getTimer() {
        if(context != null) {
            Settings.setContext(context);
        }
        return timer;
    }

    public void setSound(Boolean _sound) {
        sound = _sound;
        //preferences.edit().putBoolean(KSound, _sound).apply();
    }

    public static synchronized void setVibration(Boolean _vibration) {
        vibration = _vibration;
        preferences.edit().putBoolean(kVibration, vibration).apply();
    }

    public static synchronized void setIgnoreAllDayEvent(Boolean _ignoreAllDayEvent) {
        ignoreAllDayEvent = _ignoreAllDayEvent;
        involvesEvensListReloadByChangingIgnoreAllDayEvents = true;
        preferences.edit().putBoolean(kIgnoreAllDayEvent, ignoreAllDayEvent).apply();
    }

    public static synchronized void setSilentOnStatusBusy(Boolean _silentOnStatusBusy) {
        silentOnStatusBusy = _silentOnStatusBusy;
        involvesEvensListReloadByChangingStatusBusy = true;
        preferences.edit().putBoolean(kSilentOnStatusBusy, silentOnStatusBusy).apply();
    }

    public static synchronized Boolean getInvolvesEvensListReloadByChangingStatusBusy() {
        return involvesEvensListReloadByChangingStatusBusy;
    }

    public static synchronized void setInvolvesEvensListReloadByChangingStatusBusy(Boolean involvesEvensListReload) {
        involvesEvensListReloadByChangingStatusBusy = involvesEvensListReload;
    }

    public static synchronized Boolean getInvolvesEvensListReloadByChangingIgnoreAllDayEvents() {
        return involvesEvensListReloadByChangingIgnoreAllDayEvents;
    }

    public static synchronized void setInvolvesEvensListReloadByChangingIgnoreAllDayEvents(Boolean _involvesEvensListReloadByChangingIgnoreAllDayEvents) {
        involvesEvensListReloadByChangingIgnoreAllDayEvents = _involvesEvensListReloadByChangingIgnoreAllDayEvents;
    }

    public static synchronized void setListMeetingsForDay(Boolean _listMeetingsForDay) {
        listMeetingsForDay = _listMeetingsForDay;
        preferences.edit().putBoolean(kListMeetingsForDay, listMeetingsForDay).apply();
    }

    public static synchronized void setTimer(Boolean _timer) {
        timer = _timer;
        preferences.edit().putBoolean(kTimer, timer).apply();
    }

    public Context getContext() {
        return context;
    }

    public static synchronized void setContext(Context _context) {
        context = _context;

        if(context != null) {
            Boolean readPreferences = false;
            readPreferences = true;
            preferences = context.getSharedPreferences(
                    "com.example.app", Context.MODE_MULTI_PROCESS);
            if(readPreferences) {
                getAllSettings();
            }
        }
    }

    private static synchronized void getAllSettings() {
        sound = preferences.getBoolean(kSound, false);
        vibration = preferences.getBoolean(kVibration, true);
        ignoreAllDayEvent = preferences.getBoolean(kIgnoreAllDayEvent, true);
        silentOnStatusBusy = preferences.getBoolean(kSilentOnStatusBusy, false);
        listMeetingsForDay = preferences.getBoolean(kListMeetingsForDay, true);
        timer = preferences.getBoolean(kTimer, true);
    }
}
