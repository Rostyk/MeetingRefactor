package com.exchange.ross.exchangeapp.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.api.services.calendar.Calendar;

/**
 * Created by ross on 3/21/15.
 */
public class ApplicationContextProvider extends Application {
    /**
     * Keeps a reference of the application context
     */
    private static Context sContext;
    private static Activity sActivity;
    private static com.google.api.services.calendar.Calendar client;

    public static void setClient(Calendar client) {
        ApplicationContextProvider.client = client;
    }

    public static Calendar getClient() {

        return client;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
    }

    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {
        return sContext;
    }

    public static void setApplicationContext(Context context) {
        sContext = context;
    }

    public static Context getsContext() {
        return sContext;
    }

    public static void setActivity(Activity eventsActivity) {
        sActivity = eventsActivity;
    }

    public static Activity getActivity() {
        return sActivity;
    }
}
