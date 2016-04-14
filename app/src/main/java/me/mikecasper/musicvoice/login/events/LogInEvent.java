package me.mikecasper.musicvoice.login.events;

import android.app.Activity;

public class LogInEvent {
    private Activity mActivity;

    public LogInEvent(Activity activity) {
        mActivity = activity;
    }

    public Activity getActivity() {
        return mActivity;
    }
}
