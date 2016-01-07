package com.exchange.ross.exchangeapp.Utils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by ross on 5/24/15.
 */
public class GATracker {
    private static GoogleAnalytics analytics;
    private static Tracker tracker;
    private static GATracker instance;

    public static synchronized GATracker tracker() {
        if (instance == null)
            instance = new GATracker();
        return instance;
    }

    private GATracker() {
        analytics = GoogleAnalytics.getInstance(ApplicationContextProvider.getContext());
        tracker = analytics.newTracker("UA-62153136-2");
    }

    public void sendEvent(String category, String action, String label) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public GATracker setScreenName(String screenName) {
        tracker.setScreenName(screenName);
        return instance;
    }

}
