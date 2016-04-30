package me.mikecasper.musicvoice;

import android.app.Application;

import com.squareup.picasso.Picasso;

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
    }
}
