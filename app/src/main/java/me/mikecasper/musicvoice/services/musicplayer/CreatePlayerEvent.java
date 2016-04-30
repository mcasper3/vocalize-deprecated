package me.mikecasper.musicvoice.services.musicplayer;

import android.content.Context;

public class CreatePlayerEvent {
    private Context mContext;
    private String mToken;

    public CreatePlayerEvent(Context context, String token) {
        this.mContext = context;
        this.mToken = token;
    }

    public Context getContext() {
        return mContext;
    }

    public String getToken() {
        return mToken;
    }
}
