package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.track.TrackFragment;

public class NowPlayingActivity extends MusicVoiceActivity {

    private static final int MODE_DISABLED = 0;
    private static final int MODE_ENABLED = 1;
    private static final int MODE_SINGLE = 2;

    // Services
    private IEventManager mEventManager;

    // Data Members
    private boolean mIsPlayingMusic;
    private boolean mShuffleEnabled;
    private int mRepeatMode;

    public NowPlayingActivity() {
        // Required empty public constructor
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        mEventManager = EventManagerProvider.getInstance(this);

        setUpButtons();

        ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        TextView trackName = (TextView) findViewById(R.id.trackName);
        TextView artistName = (TextView) findViewById(R.id.artistName);
        TextView currentTime = (TextView) findViewById(R.id.currentTime);
        TextView remainingTime = (TextView) findViewById(R.id.remainingTime);

        Intent intent = getIntent();
        Track track = intent.getParcelableExtra(TrackFragment.TRACK);

        if (track != null) {
            Picasso.with(this)
                    .load(track.getAlbum().getImages().get(0).getUrl())
                    .placeholder(R.drawable.default_playlist)
                    .error(R.drawable.default_playlist)
                    .into(albumArt);

            trackName.setText(track.getName());
            artistName.setText(track.getArtists().get(0).getName());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpButtons() {
        View playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new TogglePlaybackEvent());
                updatePlayButton();
            }
        });

        View skipForwardButton = findViewById(R.id.skipNextButton);
        skipForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipForwardEvent());
            }
        });

        View skipBackwardButton = findViewById(R.id.skipPreviousButton);
        skipBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipBackwardEvent());
            }
        });

        View repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new ToggleRepeatEvent());
                updateRepeatButton();
            }
        });

        View shuffleButton = findViewById(R.id.shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new ToggleShuffleEvent());
                updateShuffleButton();
            }
        });
    }

    private void updateShuffleButton() {
        ImageView shuffleButton = (ImageView) findViewById(R.id.shuffleButton);

        if (shuffleButton != null) {
            if (mShuffleEnabled) {
                shuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
            } else {
                shuffleButton.setImageResource(R.drawable.ic_shuffle);
            }

            mShuffleEnabled = !mShuffleEnabled;
            // TODO update in shared prefs
        }
    }

    private void updateRepeatButton() {
        ImageView repeatButton = (ImageView) findViewById(R.id.repeatButton);

        if (repeatButton != null) {
            if (mRepeatMode == MODE_DISABLED) {
                repeatButton.setImageResource(R.drawable.ic_repeat);
                mRepeatMode = MODE_ENABLED;
            } else if (mRepeatMode == MODE_ENABLED) {
                repeatButton.setImageResource(R.drawable.ic_repeat_song);
                mRepeatMode = MODE_SINGLE;
            } else {
                repeatButton.setImageResource(R.drawable.ic_repeat_disabled);
                mRepeatMode = MODE_DISABLED;
            }

            // TODO update shared prefs
        }
    }

    @Override
    public void onPause() {
        mEventManager.unregister(this);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mEventManager.register(this);
    }

    private void updatePlayButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton();
        } else {
            int id = mIsPlayingMusic ? R.drawable.ic_pause : R.drawable.ic_play;
            ImageView button = (ImageView) findViewById(R.id.playPauseButton);

            if (button != null) {
                button.setImageResource(id);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton() {
        AnimatedVectorDrawable drawable;
        if (mIsPlayingMusic) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause);
        }

        ImageView button = (ImageView) findViewById(R.id.playPauseButton);

        if (button != null) {
            button.setImageDrawable(drawable);
            drawable.start();
        }

        mIsPlayingMusic = !mIsPlayingMusic;
    }
}
