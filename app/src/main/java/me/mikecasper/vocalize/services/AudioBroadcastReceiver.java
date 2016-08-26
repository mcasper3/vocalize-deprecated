package me.mikecasper.vocalize.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.events.PauseListeningEvent;
import me.mikecasper.vocalize.services.musicplayer.events.PauseMusicEvent;

public class AudioBroadcastReceiver extends BroadcastReceiver {
    public AudioBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        IEventManager eventManager = EventManagerProvider.getInstance(context);
        eventManager.postEvent(new PauseMusicEvent());
        eventManager.postEvent(new PauseListeningEvent());
    }
}
