package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.StopSeekbarUpdateEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.util.DateUtility;

public class NowPlayingActivity extends MusicVoiceActivity {

    // Constants for repeat mode
    public static final int MODE_DISABLED = 0;
    public static final int MODE_ENABLED = 1;
    public static final int MODE_SINGLE = 2;

    // Constants for storing preferences
    public static final String SHUFFLE_ENABLED = "shuffleEnabled";
    public static final String REPEAT_MODE = "repeatMode";

    // Constants for Saving View State
    private static final String IS_PLAYING = "isPlaying";

    // Constants for intents
    public static final String TRACK = "track";
    public static final String SHOULD_PLAY_TRACK = "shouldPlayTrack";
    public static final String IS_PLAYING_MUSIC = "isPlayingMusic";
    public static final String CURRENT_TIME = "currentTime";

    // Services
    private IEventManager mEventManager;

    // Data Members
    private boolean mIsPlayingMusic;
    private boolean mShuffleEnabled;
    private int mRepeatMode;
    private SeekBar mSeekBar;

    // For Updating SeekBar


    public NowPlayingActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_PLAYING, mIsPlayingMusic);
        outState.putBoolean(SHUFFLE_ENABLED, mShuffleEnabled);
        outState.putInt(REPEAT_MODE, mRepeatMode);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mEventManager = EventManagerProvider.getInstance(this);

        Intent intent = getIntent();
        Track track = intent.getParcelableExtra(TRACK);

        boolean shouldPlaySong = intent.getBooleanExtra(SHOULD_PLAY_TRACK, false);
        mIsPlayingMusic = intent.getBooleanExtra(IS_PLAYING_MUSIC, false);

        if (savedInstanceState == null) {
            if (shouldPlaySong) {
                mIsPlayingMusic = true;
            }
        } else {
            mIsPlayingMusic = savedInstanceState.getBoolean(IS_PLAYING);
            mShuffleEnabled = savedInstanceState.getBoolean(SHUFFLE_ENABLED);
            mRepeatMode = savedInstanceState.getInt(REPEAT_MODE);
        }

        setUpButtons();

        mSeekBar = (SeekBar) findViewById(R.id.song_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCurrentTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mEventManager.postEvent(new StopSeekbarUpdateEvent());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mEventManager.postEvent(new SeekToEvent(seekBar.getProgress()));
            }
        });

        if (track != null) {
            updateView(track);
        }

        int currentTime = intent.getIntExtra(CURRENT_TIME, 0);
        mSeekBar.setProgress(currentTime);
    }

    private void updateCurrentTime(int position) {
        String time = DateUtility.formatDuration(position);

        TextView currentTime = (TextView) findViewById(R.id.current_time);
        if (currentTime != null) {
            currentTime.setText(time);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpButtons() {
        ImageView playPauseButton = (ImageView) findViewById(R.id.play_pause_button);

        if (mIsPlayingMusic) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPlayingMusic = !mIsPlayingMusic;
                mEventManager.postEvent(new TogglePlaybackEvent());

                updatePlayButton();
            }
        });

        View skipForwardButton = findViewById(R.id.skip_next_button);
        skipForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipForwardEvent());

                ImageView repeatButton = (ImageView) findViewById(R.id.repeat_button);

                if (mRepeatMode == MODE_SINGLE) {
                    mRepeatMode = MODE_ENABLED;
                    repeatButton.setImageResource(R.drawable.ic_repeat);
                }
            }
        });

        View skipBackwardButton = findViewById(R.id.skip_previous_button);
        skipBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipBackwardEvent());

                ImageView repeatButton = (ImageView) findViewById(R.id.repeat_button);

                if (mRepeatMode == MODE_SINGLE) {
                    mRepeatMode = MODE_ENABLED;
                    repeatButton.setImageResource(R.drawable.ic_repeat);

                    PreferenceManager.getDefaultSharedPreferences(NowPlayingActivity.this)
                            .edit()
                            .putInt(REPEAT_MODE, mRepeatMode)
                            .apply();
                }
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mRepeatMode = preferences.getInt(REPEAT_MODE, MODE_DISABLED);
        mShuffleEnabled = preferences.getBoolean(SHUFFLE_ENABLED, false);

        ImageView repeatButton = (ImageView) findViewById(R.id.repeat_button);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new ToggleRepeatEvent());
                updateRepeatButton();
            }
        });

        if (mRepeatMode == MODE_ENABLED) {
            repeatButton.setImageResource(R.drawable.ic_repeat);
        } else if (mRepeatMode == MODE_SINGLE) {
            repeatButton.setImageResource(R.drawable.ic_repeat_song);
        }

        ImageView shuffleButton = (ImageView) findViewById(R.id.shuffle_button);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new ToggleShuffleEvent());
                updateShuffleButton();
            }
        });

        if (mShuffleEnabled) {
            shuffleButton.setImageResource(R.drawable.ic_shuffle);
        }
    }

    private void updateShuffleButton() {
        ImageView shuffleButton = (ImageView) findViewById(R.id.shuffle_button);

        if (shuffleButton != null) {
            if (mShuffleEnabled) {
                shuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
            } else {
                shuffleButton.setImageResource(R.drawable.ic_shuffle);
            }

            mShuffleEnabled = !mShuffleEnabled;

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(SHUFFLE_ENABLED, mShuffleEnabled)
                    .apply();
        }
    }

    private void updateRepeatButton() {
        ImageView repeatButton = (ImageView) findViewById(R.id.repeat_button);

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

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putInt(REPEAT_MODE, mRepeatMode)
                    .apply();
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
        mEventManager.postEvent(new GetPlayerStatusEvent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onPlayerStatusObtained(UpdatePlayerStatusEvent event) {
        boolean wasPlaying = mIsPlayingMusic;
        mIsPlayingMusic = event.isPlaying();

        if (wasPlaying != mIsPlayingMusic) {
            updatePlayButton();
        }

        Track track = event.getTrack();

        if (track != null) {
            Intent intent = getIntent();
            intent.putExtra(TRACK, track);

            updateView(track);
        }
    }

    @Subscribe
    public void onSongChange(SongChangeEvent event) {
        Track track = event.getTrack();

        Intent intent = getIntent();
        intent.putExtra(TRACK, track);

        boolean wasPreviouslyPlaying = mIsPlayingMusic;
        mIsPlayingMusic = event.isPlayingSong();

        if (wasPreviouslyPlaying != mIsPlayingMusic) {
            updatePlayButton();
        }

        TextView trackName = (TextView) findViewById(R.id.track_name);
        if (trackName != null && !trackName.getText().toString().equals(track.getName())) {

            TextView currentTime = (TextView) findViewById(R.id.current_time);
            if (currentTime != null) {
                currentTime.setText(R.string.initial_time);
            }

            updateView(track);
        }
    }

    @Subscribe
    public void onLostPermission(LostPermissionEvent event) {
        if (mIsPlayingMusic) {
            mIsPlayingMusic = false;
            updatePlayButton();
        }
    }

    private void updateView(Track track) {
        mSeekBar.setMax(track.getDuration());

        ImageView albumArt = (ImageView) findViewById(R.id.album_art);
        TextView trackName = (TextView) findViewById(R.id.track_name);
        TextView artistName = (TextView) findViewById(R.id.artist_name);
        TextView remainingTime = (TextView) findViewById(R.id.remaining_time);

        Picasso.with(this)
                .load(track.getAlbum().getImages().get(0).getUrl())
                .error(R.drawable.default_playlist)
                .into(albumArt);

        if (trackName != null && artistName != null && remainingTime != null) {
            trackName.setText(track.getName());
            remainingTime.setText(DateUtility.formatDuration(track.getDuration()));

            String artistNames = "";

            for (Artist artist : track.getArtists()) {
                artistNames += artist.getName() + ", ";
            }

            artistNames = artistNames.substring(0, artistNames.length() - 2);

            artistName.setText(artistNames);
        }

    }

    private void updatePlayButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton();
        } else {
            int id = mIsPlayingMusic ? R.drawable.ic_pause : R.drawable.ic_play;
            ImageView button = (ImageView) findViewById(R.id.play_pause_button);

            if (button != null) {
                button.setImageResource(id);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton() {
        AnimatedVectorDrawable drawable;
        if (mIsPlayingMusic) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play);
        }

        ImageView button = (ImageView) findViewById(R.id.play_pause_button);

        if (button != null) {
            button.setImageDrawable(drawable);
            drawable.start();
        }
    }

    // Updating SeekBar
    @Subscribe
    public void updateProgress(UpdateSongTimeEvent event) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(event.getSongTime());
        }
    }
}
