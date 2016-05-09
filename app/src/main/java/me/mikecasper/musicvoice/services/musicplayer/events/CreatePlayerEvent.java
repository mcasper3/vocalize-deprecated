package me.mikecasper.musicvoice.services.musicplayer.events;

import android.content.Context;

public class CreatePlayerEvent {
    private Context mContext;
    private String mToken;
    private boolean mShuffleEnabled;
    private int mRepeatMode;

    public CreatePlayerEvent(Context context, String token, boolean shuffleEnabled, int repeatMode) {
        this.mContext = context;
        this.mToken = token;
        this.mShuffleEnabled = shuffleEnabled;
        this.mRepeatMode = repeatMode;
    }

    public Context getContext() {
        return mContext;
    }

    public String getToken() {
        return mToken;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public boolean isShuffleEnabled() {
        return mShuffleEnabled;
    }
}
