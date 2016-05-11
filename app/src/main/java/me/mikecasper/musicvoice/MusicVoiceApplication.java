package me.mikecasper.musicvoice;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.DestroyPlayerEvent;

public class MusicVoiceApplication extends Application {

    public static final LogLevel LOG_LEVEL = LogLevel.DEBUG;

    public enum LogLevel {
        FULL,
        DEBUG,
        BASIC,
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }

    @Override
    public void onTerminate() {
        IEventManager eventManager = EventManagerProvider.getInstance(this);
        eventManager.postEvent(new DestroyPlayerEvent());

        super.onTerminate();
    }
}
