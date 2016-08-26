package me.mikecasper.vocalize.nowplaying;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import me.mikecasper.vocalize.MusicVoiceActivity;
import me.mikecasper.vocalize.MusicVoiceApplication;
import me.mikecasper.vocalize.R;
import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.MusicPlayer;
import me.mikecasper.vocalize.services.musicplayer.events.DisplayNotificationEvent;
import me.mikecasper.vocalize.services.musicplayer.events.GetPlayerStatusEvent;

public class NowPlayingActivity extends MusicVoiceActivity {

    // Constants for repeat mode
    public static final int MODE_DISABLED = 0;
    public static final int MODE_ENABLED = 1;
    public static final int MODE_SINGLE = 2;

    // Constants for storing preferences
    public static final String SHUFFLE_ENABLED = "shuffleEnabled";
    public static final String REPEAT_MODE = "repeatMode";

    // Constants for intents
    public static final String TRACK = "track";
    public static final String SHOULD_PLAY_TRACK = "shouldPlayTrack";
    public static final String IS_PLAYING_MUSIC = "isPlayingMusic";
    public static final String CURRENT_TIME = "currentTime";
    public static final String PLAYLIST_NAME = "playlistName";

    // Services
    private IEventManager mEventManager;

    public NowPlayingActivity() {
        // Required empty public constructor
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mEventManager = EventManagerProvider.getInstance(this);

        if (savedInstanceState == null) {
            Fragment fragment = new NowPlayingFragment();

            Intent intent = getIntent();
            Bundle args = intent.getExtras();

            if (args == null) {
                args = new Bundle();
            }

            fragment.setArguments(args);

            // start first activity
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.now_playing_content, fragment)
                    .commit();
        }
    }

    @Override
    public void onPause() {
        mEventManager.postEvent(new DisplayNotificationEvent());
        mEventManager.unregister(this);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mEventManager.register(this);

        if (!MusicPlayer.isAlive()) {
            Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);
            intent.setAction(MusicPlayer.CREATE_PLAYER);
            startService(intent);
        }

        mEventManager.postEvent(new GetPlayerStatusEvent());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        mEventManager = null;

        ((MusicVoiceApplication) getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }
}
