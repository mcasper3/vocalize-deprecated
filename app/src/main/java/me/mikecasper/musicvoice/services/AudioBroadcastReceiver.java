package me.mikecasper.musicvoice.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.PauseMusicEvent;

public class AudioBroadcastReceiver extends BroadcastReceiver {
    public AudioBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        IEventManager eventManager = EventManagerProvider.getInstance(context);
        eventManager.postEvent(new PauseMusicEvent());
    }
}
