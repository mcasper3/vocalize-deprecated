package me.mikecasper.musicvoice.api.services;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.login.LogInActivity;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.login.events.LogOutEvent;
import me.mikecasper.musicvoice.util.Logger;

public class LogInService {
    private static final String CLIENT_ID = "6efaf35f4aa84d029e9a319eebb73211";
    private static final String REDIRECT_URI = "musicvoice-request://callback";

    public static final String SPOTIFY_TOKEN = "spotifyToken";
    public static final String LOGIN_EXPIRATION_TIME = "loginExpirationTime";
    public static final String LAST_LOGIN_TIME = "lastLoginTime";
    public static final int LOGIN_REQUEST_CODE = 9001;

    public LogInService() {

    }

    @Subscribe
    public void onLogIn(LogInEvent event) {
        Logger.i("LogInService", "Received request");
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(event.getActivity(), LOGIN_REQUEST_CODE, request);
    }

    @Subscribe
    public void onLogOut(LogOutEvent event) {
        Context context = event.getContext();

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LogInActivity.IS_LOGGED_IN, false)
                .putString(SPOTIFY_TOKEN, null)
                .putLong(LAST_LOGIN_TIME, 0)
                .putInt(LOGIN_EXPIRATION_TIME, 0)
                .apply();

        Intent intent = new Intent(context, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
