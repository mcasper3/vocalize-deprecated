package me.mikecasper.musicvoice.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.login.events.GetUserEvent;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.MainActivity;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.CreatePlayerEvent;
import me.mikecasper.musicvoice.util.Logger;

public class LogInActivity extends MusicVoiceActivity {

    public static final String IS_LOGGED_IN = "isLoggedIn";

    private static final String TAG = "LogInActivity";
    private IEventManager mEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);

        mEventManager = EventManagerProvider.getInstance(this);

        if (isLoggedIn) {
            boolean shuffleEnabled = sharedPreferences.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED, false);
            int repeatMode = sharedPreferences.getInt(NowPlayingActivity.REPEAT_MODE, 0);

            String token = sharedPreferences.getString(LogInService.SPOTIFY_TOKEN, null);
            mEventManager.postEvent(new CreatePlayerEvent(this, token, shuffleEnabled, repeatMode));
            moveToMainView();
        }
    }

    public void onLogIn(View view) {
        mEventManager.postEvent(new LogInEvent(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LogInService.LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    Logger.i(TAG, "Logged in");
                    moveToMainView();
                    break;
                case ERROR:
                    Logger.e(TAG, response.getError());
                    Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }

    private void moveToMainView() {
        mEventManager.postEvent(new GetUserEvent());

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
