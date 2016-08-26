package me.mikecasper.vocalize.services.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaSessionCompat;

import me.mikecasper.vocalize.util.Logger;

public class MediaPlayerCallback extends MediaSessionCompat.Callback {
    @Override
    public void onPlay() {
        super.onPlay();

        Logger.e("Callback", "Doing something yo 3");
    }

    @Override
    public void onCommand(String command, Bundle extras, ResultReceiver cb) {
        super.onCommand(command, extras, cb);

        Logger.e("Callback", "Doing something yo 2");
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        Logger.e("Callback", "Doing something yo");
        return super.onMediaButtonEvent(mediaButtonEvent);
    }
}
