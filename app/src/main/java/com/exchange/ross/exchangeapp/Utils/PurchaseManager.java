package com.exchange.ross.exchangeapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.ContextMenu;

import com.exchange.ross.exchangeapp.Utils.billing.IabHelper;
import com.exchange.ross.exchangeapp.Utils.billing.IabResult;
import com.exchange.ross.exchangeapp.Utils.billing.Inventory;
import com.exchange.ross.exchangeapp.Utils.billing.OnPurchased;
import com.exchange.ross.exchangeapp.Utils.billing.Purchase;

/**
 * Created by ross on 4/23/15.
 */
public class PurchaseManager {
    private OnPurchased purchased;
    private String kAlreadyOwned = "kAlreadyOwned";
    private Boolean alreadyOwned;
    private static final String TAG =
            "billings";
    private SharedPreferences preferences;
    static final String ITEM_SKU = "android.test.purchased";
    public IabHelper mHelper;

    private static PurchaseManager instance;
    public static synchronized PurchaseManager sharedManager() {
        if (instance == null) {
            instance = new PurchaseManager();
        }
        return instance;
    }

    public void init(Context context) {

        preferences = context.getSharedPreferences(
                "com.example.app", Context.MODE_PRIVATE);
        getPreferences();

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp6Y6lfvOv7vIXtBmz3ZqGCM6QffjWD5giQxWAGAYI9Dp1mzKgb2chE8sP7exzVfJXHnfvELDv9TtX9Y3qpOx0Ekg5cWGjCZ/MChY8Oy5Hq9fyAvh5ohzPIOQjlcLJuJeAWRDeenjMnQsQkkCIvfkuyeokwTr3w+qKvexqUcERBedfrEzV5My6YOajMXa3ypZnxC7LkO2QpgH0I72gdmaZAZ2wQLDGzZw3k7XpJy8rOiEqLbsAuVje/Tvtq/xuStgNEEbY21QKby7OlZ9PccAuAAgvLixL9tOihdz+awDxiMeHeZBkmCJt9AL7Y6voyH83I+3jZvjJWAjzLdq+G9kgwIDAQAB";

        mHelper = new IabHelper(context, base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result)
                                       {
                                           if (!result.isSuccess()) {
                                               Log.d(TAG, "In-app Billing setup failed: " +
                                                       result);
                                           } else {
                                               Log.d(TAG, "In-app Billing is set up OK");
                                           }
                                       }
                                   });
    }

    public void buy(OnPurchased purchased, Activity activity) {
        this.purchased = purchased;
        mHelper.launchPurchaseFlow(activity, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {

            int res = result.getResponse();
            if(res == 7) {
                alreadyOwned = true;

                if(purchase != null)
                    mHelper.consumeAsync(purchase, mConsumeFinishListener);

                alreadyOwned = true;
                purchased.onPurchaseComplete(true);
            }

            if (result.isFailure()) {
               GATracker.tracker().setScreenName("Settings").sendEvent("Purchase", "Failure. " + result.getMessage(), "");
               return;
            }

            alreadyOwned = true;
            GATracker.tracker().setScreenName("Settings").sendEvent("Purchase", "Success", "");
            purchased.onPurchaseComplete(true);
        }

    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if(result.isFailure()) {

            }
            else {
                Log.v("Purchase", "consumed success");
            }
        }
    };

    private void getPreferences() {
        this.alreadyOwned = preferences.getBoolean(kAlreadyOwned, false);
    }

    public Boolean getAlreadyOwned() {
        return alreadyOwned;
    }

    public void setAlreadyOwned(Boolean alreadyOwned) {
        this.alreadyOwned = alreadyOwned;
        preferences.edit().putBoolean(kAlreadyOwned, alreadyOwned).apply();
    }
}
