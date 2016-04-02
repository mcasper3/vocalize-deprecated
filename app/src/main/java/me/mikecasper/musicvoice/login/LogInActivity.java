package me.mikecasper.musicvoice.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Date;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.requests.GetPlaylistsRequest;
import me.mikecasper.musicvoice.api.requests.GetUserRequest;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.MainActivity;
import me.mikecasper.musicvoice.services.EventManager;
import me.mikecasper.musicvoice.services.EventManagerProvider;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LogInActivity";
    private EventManager mEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mEventManager = EventManagerProvider.getInstance(this);
        ImageView logInButton = (ImageView) findViewById(R.id.logInButton);

        if (logInButton != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Drawable image = ContextCompat.getDrawable(this, R.drawable.log_in_button);
                RippleDrawable drawable = new RippleDrawable(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.spotify_green_pressed)), image, null);
                logInButton.setImageDrawable(drawable);
            }

            logInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLogIn();
                }
            });
        }
    }

    private void onLogIn() {
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
                            .putLong(LogInService.LAST_LOGIN_TIME, new Date().getTime())
                            .putInt(LogInService.LOGIN_EXPIRATION_TIME, response.getExpiresIn())
                            .putString(LogInService.SPOTIFY_TOKEN, response.getAccessToken())
                            .apply();

                    Log.i(TAG, "Logged in");
                    mEventManager.postEvent(new GetUserRequest());
                    mEventManager.postEvent(new GetPlaylistsRequest());

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    break;
                case ERROR:
                    Log.e(TAG, response.getError());
                    Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }
}
