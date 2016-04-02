package me.mikecasper.musicvoice.services.eventmanager;

import android.content.Context;

public class EventManagerProvider {
    private static EventManager instance;

    private EventManagerProvider() { }

    public static EventManager getInstance(Context context) {
        if (instance == null) {
            instance = new EventManager(context.getApplicationContext());
        }

        return instance;
    }
}
