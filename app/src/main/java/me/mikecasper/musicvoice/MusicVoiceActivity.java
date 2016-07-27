package me.mikecasper.musicvoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.login.LogInActivity;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.MusicPlayer;

public class MusicVoiceActivity extends AppCompatActivity {
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LogInService.LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    sharedPreferences.edit()
                            .putLong(LogInService.LAST_LOGIN_TIME, System.currentTimeMillis())
                            // Convert to ms before storing
                            .putInt(LogInService.LOGIN_EXPIRATION_TIME, response.getExpiresIn() * 1000)
                            .putString(LogInService.SPOTIFY_TOKEN, response.getAccessToken())
                            .putBoolean(LogInActivity.IS_LOGGED_IN, true)
                            .apply();

                    Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);
                    intent.setAction(MusicPlayer.CREATE_PLAYER);
                    startService(intent);

                    break;
                case ERROR:
                    // TODO back to log in screen
                    break;
                default:
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!(this instanceof LogInActivity) && !(this instanceof SplashActivity)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            long lastLoginTime = sharedPreferences.getLong(LogInService.LAST_LOGIN_TIME, 0);
            int expirationTime = sharedPreferences.getInt(LogInService.LOGIN_EXPIRATION_TIME, 0);

            IEventManager eventManager = EventManagerProvider.getInstance(this);
            if (System.currentTimeMillis() > lastLoginTime + expirationTime) {
                eventManager.postEvent(new LogInEvent(this));
            } else {
                if (!MusicPlayer.isAlive()) {
                    Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);
                    intent.setAction(MusicPlayer.CREATE_PLAYER);
                    startService(intent);
                }
            }
        }
    }
}
