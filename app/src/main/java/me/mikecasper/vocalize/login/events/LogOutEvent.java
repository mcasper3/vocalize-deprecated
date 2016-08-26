package me.mikecasper.vocalize.login.events;

import android.content.Context;

public class LogOutEvent {
    private Context mContext;

    public LogOutEvent(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }
}
