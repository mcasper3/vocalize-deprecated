package me.mikecasper.musicvoice.services;

import android.util.Log;

import com.squareup.otto.Bus;

import me.mikecasper.musicvoice.login.LogInService;
import me.mikecasper.musicvoice.events.spotify.SpotifyEvent;

public class ApplicationEventManager {

    private static final String TAG = "ApplicationEventManager";

    private Bus mBus;
    private LogInService mLogInService;

    ApplicationEventManager() {
        mBus = BusProvider.getBus();
        mLogInService = new LogInService();

        subscribeServices();
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
            Log.e(TAG, "Error un-subscribing object", e);
        }
    }

    public void postEvent(Object object) {
        if (object instanceof SpotifyEvent) {
            // todo check for token expiration time and do authentication if needed
            if () {

            } else {

            }
        } else {
            mBus.post(object);
        }
    }

    private void subscribeServices() {
        mBus.register(mLogInService);
    }
}
