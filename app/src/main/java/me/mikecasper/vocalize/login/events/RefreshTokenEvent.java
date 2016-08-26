package me.mikecasper.vocalize.login.events;

import retrofit2.Call;
import retrofit2.Callback;

public class RefreshTokenEvent {
    private Call mCall;
    private Callback mCallback;

    public RefreshTokenEvent(Call call, Callback callback) {
        mCall = call;
        mCallback = callback;
    }

    public Call getCall() {
        return mCall;
    }

    public Callback getCallback() {
        return mCallback;
    }
}
