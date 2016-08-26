package me.mikecasper.vocalize;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import me.mikecasper.vocalize.api.services.LogInService;
import me.mikecasper.vocalize.login.LogInActivity;
import me.mikecasper.vocalize.login.events.LogInEvent;
import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.MusicPlayer;

public class SplashActivity extends MusicVoiceActivity {

    private IEventManager mEventManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventManager = EventManagerProvider.getInstance(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isLoggedIn = sharedPreferences.getBoolean(LogInActivity.IS_LOGGED_IN, false);

        if (isLoggedIn) {
            logIn(sharedPreferences);
        } else {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
        }
    }

    private void logIn(SharedPreferences sharedPreferences) {
        long lastLoginTime = sharedPreferences.getLong(LogInService.LAST_LOGIN_TIME, 0);
        int expirationTime = sharedPreferences.getInt(LogInService.LOGIN_EXPIRATION_TIME, 0);

        if (System.currentTimeMillis() > lastLoginTime + expirationTime) {
            mEventManager.postEvent(new LogInEvent(this));
        } else {
            Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);
            intent.setAction(MusicPlayer.CREATE_PLAYER);
            startService(intent);

            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LogInService.LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    Intent mainActivityIntent = new Intent(this, MainActivity.class);
                    startActivity(mainActivityIntent);
                    break;
                case ERROR:
                    Intent logInActivityIntent = new Intent(this, LogInActivity.class);
                    startActivity(logInActivityIntent);
                    break;
                default:
            }
        }
    }

    @Override
    protected void onDestroy() {
        mEventManager = null;

        super.onDestroy();
    }
}
