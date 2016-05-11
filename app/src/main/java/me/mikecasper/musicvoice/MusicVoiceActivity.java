package me.mikecasper.musicvoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.login.LogInActivity;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.MusicPlayer;
import me.mikecasper.musicvoice.services.musicplayer.events.CreatePlayerEvent;

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

                    boolean shuffleEnabled = sharedPreferences.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED, false);
                    int repeatMode = sharedPreferences.getInt(NowPlayingActivity.REPEAT_MODE, 0);

                    Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);
                    intent.setAction(MusicPlayer.CREATE_PLAYER);
                    intent.putExtra(LogInService.SPOTIFY_TOKEN, response.getAccessToken());
                    intent.putExtra(NowPlayingActivity.SHUFFLE_ENABLED, shuffleEnabled);
                    intent.putExtra(NowPlayingActivity.REPEAT_MODE, repeatMode);
                    startService(intent);

                    //IEventManager eventManager = EventManagerProvider.getInstance(this);
                    //eventManager.postEvent(new CreatePlayerEvent(this, response.getAccessToken(), shuffleEnabled, repeatMode));
                    break;
                case ERROR:
                    // TODO back to log in screen
                    break;
                default:
            }
        }
    }
}
