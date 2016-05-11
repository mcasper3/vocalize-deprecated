package me.mikecasper.musicvoice;

import android.app.Application;
import android.content.Intent;

import com.squareup.leakcanary.LeakCanary;

import me.mikecasper.musicvoice.services.musicplayer.MusicPlayer;
import me.mikecasper.musicvoice.util.Logger;

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
        Logger.e("FDDS:LJ", "FJDKSLJF");
        Intent intent = new Intent(this, MusicPlayer.class);
        intent.setAction(MusicPlayer.DESTROY_PLAYER);
        stopService(intent);

        super.onTerminate();
    }
}
