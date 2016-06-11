package me.mikecasper.musicvoice.nowplaying;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.MusicPlayer;
import me.mikecasper.musicvoice.services.musicplayer.events.DisplayNotificationEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.util.Logger;

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Logger.d("NowPlayingActivity", "In new intent");
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
