package me.mikecasper.musicvoice.services.eventmanager;

import android.content.Context;

public class EventManagerProvider {
    private static IEventManager sInstance;
    private static final boolean sIsTesting = true;

    private EventManagerProvider() { }

    public static IEventManager getInstance(Context context) {
        if (sInstance == null) {
            if (!sIsTesting) {
                sInstance = new EventManager(context.getApplicationContext());
            } else {
                sInstance = new MockEventManager();
            }
        }

        return sInstance;
    }
}
