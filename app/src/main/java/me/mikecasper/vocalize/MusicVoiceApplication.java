package me.mikecasper.vocalize;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class MusicVoiceApplication extends Application {

    public static final LogLevel LOG_LEVEL = LogLevel.DEBUG;
    private RefWatcher mRefWatcher;

    public RefWatcher getRefWatcher() {
        return mRefWatcher;
    }

    public enum LogLevel {
        FULL,
        DEBUG,
        BASIC,
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRefWatcher = LeakCanary.install(this);
    }
}
