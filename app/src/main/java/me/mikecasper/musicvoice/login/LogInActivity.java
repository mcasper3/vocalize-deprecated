package me.mikecasper.musicvoice.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.playlist.events.GetPlaylistsEvent;
import me.mikecasper.musicvoice.login.events.GetUserEvent;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.MainActivity;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.util.Logger;

public class LogInActivity extends AppCompatActivity {

    public static final String IS_LOGGED_IN = "isLoggedIn";

    private static final String TAG = "LogInActivity";
    private IEventManager mEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        boolean isLoggedIn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(IS_LOGGED_IN, false);

        mEventManager = EventManagerProvider.getInstance(this);

        if (isLoggedIn) {
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
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    sharedPreferences.edit()
                            .putLong(LogInService.LAST_LOGIN_TIME, System.currentTimeMillis())
                            .putInt(LogInService.LOGIN_EXPIRATION_TIME, response.getExpiresIn())
                            .putString(LogInService.SPOTIFY_TOKEN, response.getAccessToken())
                            .putBoolean(IS_LOGGED_IN, true)
                            .apply();

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
        mEventManager.postEvent(new GetPlaylistsEvent());

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
