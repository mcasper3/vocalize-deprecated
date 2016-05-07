package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.PlaySongEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.util.DateUtility;

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

    // Services
    private IEventManager mEventManager;

    // Data Members
    private boolean mIsPlayingMusic;
    private boolean mShuffleEnabled;
    private int mRepeatMode;
    private SeekBar mSeekBar;

    // For Updating SeekBar
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private ScheduledFuture<?> mScheduledFuture;
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();
    private long mPreviousTime;
    private int mPreviousSongTime;
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    public NowPlayingActivity() {
        // Required empty public constructor
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        mEventManager = EventManagerProvider.getInstance(this);

        Intent intent = getIntent();
        final Track track = intent.getParcelableExtra(TRACK);

        boolean shouldPlaySong = intent.getBooleanExtra(SHOULD_PLAY_TRACK, false);

        if (shouldPlaySong) {
            mIsPlayingMusic = true;
            mPreviousTime = SystemClock.elapsedRealtime();
            mPreviousSongTime = 0;
            mEventManager.postEvent(new PlaySongEvent());
            scheduleSeekBarUpdate();
        }

        setUpButtons();

        mSeekBar = (SeekBar) findViewById(R.id.songSeekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCurrentTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekBarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mEventManager.postEvent(new SeekToEvent(seekBar.getProgress()));
                mPreviousSongTime = seekBar.getProgress();
                scheduleSeekBarUpdate();
            }
        });

        if (track != null) {
            updateView(track);
        }
    }

    private void updateCurrentTime(int position) {
        String time = DateUtility.formatDuration(position);

        TextView currentTime = (TextView) findViewById(R.id.currentTime);
        if (currentTime != null) {
            currentTime.setText(time);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpButtons() {
        ImageView playPauseButton = (ImageView) findViewById(R.id.playPauseButton);

        if (mIsPlayingMusic) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new TogglePlaybackEvent());
                mIsPlayingMusic = !mIsPlayingMusic;

                if (mIsPlayingMusic) {
                    mPreviousTime = SystemClock.elapsedRealtime();
                    scheduleSeekBarUpdate();
                } else {
                    stopSeekBarUpdate();
                }

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mRepeatMode = preferences.getInt(REPEAT_MODE, MODE_DISABLED);
        mShuffleEnabled = preferences.getBoolean(SHUFFLE_ENABLED, false);

        ImageView repeatButton = (ImageView) findViewById(R.id.repeatButton);
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

        ImageView shuffleButton = (ImageView) findViewById(R.id.shuffleButton);
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
        ImageView shuffleButton = (ImageView) findViewById(R.id.shuffleButton);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSeekBarUpdate();
        mExecutorService.shutdown();
    }

    @Subscribe
    public void onSongChange(SongChangeEvent event) {
        Track track = event.getTrack();

        Intent intent = getIntent();
        intent.putExtra(TRACK, track);

        mPreviousSongTime = 0;

        mIsPlayingMusic = event.isPlayingSong();
        updatePlayButton();

        if (event.isPlayingSong()) {
            mPreviousTime = SystemClock.elapsedRealtime();
            scheduleSeekBarUpdate();
        } else {
            stopSeekBarUpdate();
        }

        TextView currentTime = (TextView) findViewById(R.id.currentTime);
        if (currentTime != null) {
            currentTime.setText(R.string.initial_time);
        }

        updateView(track);
    }

    private void updateView(Track track) {
        mSeekBar.setMax(track.getDuration());

        ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        TextView trackName = (TextView) findViewById(R.id.trackName);
        TextView artistName = (TextView) findViewById(R.id.artistName);
        TextView remainingTime = (TextView) findViewById(R.id.remainingTime);

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
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play);
        }

        ImageView button = (ImageView) findViewById(R.id.playPauseButton);

        if (button != null) {
            button.setImageDrawable(drawable);
            drawable.start();
        }
    }

    // Updating SeekBar
    private void scheduleSeekBarUpdate() {
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduledFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS
            );
        }
    }

    private void stopSeekBarUpdate() {
        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (!mIsPlayingMusic) {
            return;
        }
        long currentTime = SystemClock.elapsedRealtime();

        long difference = currentTime - mPreviousTime;
        mPreviousTime = currentTime;
        mPreviousSongTime += difference;

        if (mSeekBar != null) {
            mSeekBar.setProgress(mPreviousSongTime);
        }
    }
}
