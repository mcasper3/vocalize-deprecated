package me.mikecasper.vocalize.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.events.BeginListeningEvent;
import me.mikecasper.vocalize.services.musicplayer.events.PauseListeningEvent;

public class HeadphoneBroadcastReceiver extends BroadcastReceiver {

    private boolean mIsPluggedIn;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("state")){
            IEventManager eventManager = EventManagerProvider.getInstance(context);

            if (mIsPluggedIn && intent.getIntExtra("state", 0) == 0){
                mIsPluggedIn = false;
                eventManager.postEvent(new PauseListeningEvent());
            } else if (!mIsPluggedIn && intent.getIntExtra("state", 0) == 1){
                mIsPluggedIn = true;
                eventManager.postEvent(new BeginListeningEvent());
            }
        }
    }
}
