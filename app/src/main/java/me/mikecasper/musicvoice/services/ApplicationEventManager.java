package me.mikecasper.musicvoice.services;

import android.util.Log;

import com.squareup.otto.Bus;

public class ApplicationEventManager {

    private static final String TAG = "ApplicationEventManager";

    private Bus mBus;

    public ApplicationEventManager() {
        mBus = BusProvider.getBus();
    }

    public void subscribe(Object object) {
        try {
            mBus.register(object);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error subscribing object", e);
        }
    }

    public void unsubscribe(Object object) {
        try {
            mBus.unregister(object);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error unsubscribing object", e);
        }
    }

    public void postEvent(Object object) {
        if (object instanceof SpotifyEvent) {
            // todo check for token expiration time and do authentication if needed
        } else {

        }
    }
}
