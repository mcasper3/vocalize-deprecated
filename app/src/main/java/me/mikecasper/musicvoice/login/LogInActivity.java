package me.mikecasper.musicvoice.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.MainActivity;
import me.mikecasper.musicvoice.onboarding.OnboardingActivity;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.MusicPlayer;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.testnowplaying.AlternateNowPlaying;
import me.mikecasper.musicvoice.util.Logger;

public class LogInActivity extends MusicVoiceActivity {

    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String FIRST_LOGIN = "firstLogin";

    private static final String TAG = "LogInActivity";
    private IEventManager mEventManager;
    private boolean mLoggingIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mEventManager = EventManagerProvider.getInstance(this);

        ImageView logInBackground = (ImageView) findViewById(R.id.logInBackground);

        Picasso.with(this)
                .load(R.drawable.log_in_background_blurred)
                .into(logInBackground, new Callback() {
                    @Override
                    public void onSuccess() {
                        showBackgroundGradient();
                        showLogInButton();
                    }

                    @Override
                    public void onError() {
                        // Ignore
                        Log.e("DSF", "DSF:LKJ");
                        showBackgroundGradient();
                        showLogInButton();
                    }
                });

    }

    private void showBackgroundGradient() {
        View backgroundGradient = findViewById(R.id.background_gradient);
        backgroundGradient.setVisibility(View.VISIBLE);
    }

    private void showLogInButton() {
        View logInButton = findViewById(R.id.log_in_button);
        logInButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO look into this:
        // ViewCompat.setElevation(view, 4dp);

        mEventManager.postEvent(new GetPlayerStatusEvent());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);

        if (!mLoggingIn && isLoggedIn) {
            long lastLoginTime = sharedPreferences.getLong(LogInService.LAST_LOGIN_TIME, 0);
            int expirationTime = sharedPreferences.getInt(LogInService.LOGIN_EXPIRATION_TIME, 0);

            if (System.currentTimeMillis() > lastLoginTime + expirationTime) {
                mEventManager.postEvent(new LogInEvent(this));
            } else {
                Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);
                intent.setAction(MusicPlayer.CREATE_PLAYER);
                startService(intent);

                moveToMainView();
            }
        }

        mLoggingIn = false;
    }

    public void onLogIn(View view) {
        mEventManager.postEvent(new LogInEvent(this));
        mLoggingIn = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LogInService.LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    Logger.i(TAG, "Logged in");
                    determineNextView();
                    break;
                case ERROR:
                    Logger.e(TAG, response.getError());
                    Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }

    private void determineNextView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLogin = sharedPreferences.getBoolean(FIRST_LOGIN, false);

        boolean testing = true;

        //Intent intent = new Intent(this, AlternateNowPlaying.class);
        //startActivity(intent);

        if (firstLogin) {
            sharedPreferences.edit()
                    .putBoolean(FIRST_LOGIN, true)
                    .apply();

            moveToOnboarding();
        } else {
            moveToMainView();
        }
    }

    private void moveToOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
    }

    private void moveToMainView() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        mEventManager = null;

        super.onDestroy();
    }
}
