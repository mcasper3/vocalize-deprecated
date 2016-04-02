package me.mikecasper.musicvoice.api.services;

import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.login.events.LogInEvent;

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
        Log.i("LogInService", "Received request");
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(event.getActivity(), LOGIN_REQUEST_CODE, request);
    }
}
