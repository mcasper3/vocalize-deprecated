package me.mikecasper.musicvoice.services.eventmanager;

import android.content.Context;

public class EventManagerProvider {
    private static IEventManager sInstance;

    private EventManagerProvider() { }

    public static IEventManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EventManager(context.getApplicationContext());
        }

        return sInstance;
    }
}
