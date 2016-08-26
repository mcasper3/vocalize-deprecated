package me.mikecasper.vocalize.controllers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;

import me.mikecasper.vocalize.R;
import me.mikecasper.vocalize.nowplaying.NowPlayingActivity;
import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.vocalize.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.vocalize.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.vocalize.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.vocalize.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.vocalize.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.vocalize.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.vocalize.services.musicplayer.events.UpdatePlayerStatusEvent;

public class NowPlayingMusicControls implements MusicButtonsController {

    private Context mContext;
    private IEventManager mEventManager;
    private ImageView mPlayPauseButton;
    private ImageView mShuffleButton;
    private ImageView mRepeatButton;
    private boolean mIsPlayingMusic;
    private boolean mShuffleEnabled;
    private int mRepeatMode;

    public NowPlayingMusicControls(View view, boolean isPlayingMusic, boolean shuffleEnabled, int repeateMode) {
        mContext = view.getContext();
        mEventManager = EventManagerProvider.getInstance(mContext);

        mIsPlayingMusic = isPlayingMusic;
        mShuffleEnabled = shuffleEnabled;
        mRepeatMode = repeateMode;

        setUpButtons(view);
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpButtons(View view) {
        mPlayPauseButton = (ImageView) view.findViewById(R.id.play_pause_button);

        if (mIsPlayingMusic) {
            mPlayPauseButton.setImageResource(R.drawable.ic_pause);
        }

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPlayingMusic = !mIsPlayingMusic;
                mEventManager.postEvent(new TogglePlaybackEvent());

                updatePlayButton();
            }
        });

        View skipForwardButton = view.findViewById(R.id.skip_next_button);
        skipForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipForwardEvent());
                checkRepeatMode();
            }
        });

        View skipBackwardButton = view.findViewById(R.id.skip_previous_button);
        skipBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipBackwardEvent());
                checkRepeatMode();
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        mRepeatMode = preferences.getInt(NowPlayingActivity.REPEAT_MODE, NowPlayingActivity.MODE_DISABLED);
        mShuffleEnabled = preferences.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED, false);

        mRepeatButton = (ImageView) view.findViewById(R.id.repeat_button);
        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new ToggleRepeatEvent());
                updateRepeatButton();
            }
        });

        if (mRepeatMode == NowPlayingActivity.MODE_ENABLED) {
            mRepeatButton.setImageResource(R.drawable.ic_repeat);
        } else if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
            mRepeatButton.setImageResource(R.drawable.ic_repeat_song);
        }
        mShuffleButton = (ImageView) view.findViewById(R.id.shuffle_button);
        mShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new ToggleShuffleEvent());
                updateShuffleButton();
            }
        });

        if (mShuffleEnabled) {
            mShuffleButton.setImageResource(R.drawable.ic_shuffle);
        }
    }

    @Subscribe
    @Override
    public void onPlayerStatusObtained(UpdatePlayerStatusEvent event) {
        boolean wasPlaying = mIsPlayingMusic;
        mIsPlayingMusic = event.isPlaying();

        if (wasPlaying != mIsPlayingMusic) {
            updatePlayButton();
        }
    }

    @Subscribe
    @Override
    public void onSongChange(SongChangeEvent event) {
        boolean wasPreviouslyPlaying = mIsPlayingMusic;
        mIsPlayingMusic = event.isPlayingSong();

        if (wasPreviouslyPlaying != mIsPlayingMusic) {
            updatePlayButton();
        }
    }

    @Subscribe
    @Override
    public void onLostPermission(LostPermissionEvent event) {
        if (mIsPlayingMusic) {
            mIsPlayingMusic = false;
            updatePlayButton();
        }
    }

    @Override
    public void tearDown() {
        mRepeatButton = null;
        mShuffleButton = null;
        mContext = null;
        mEventManager = null;
        mPlayPauseButton = null;
    }

    private void updatePlayButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton();
        } else {
            int id = mIsPlayingMusic ? R.drawable.ic_pause : R.drawable.ic_play;

                    mPlayPauseButton.setImageResource(id);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton() {
        AnimatedVectorDrawable drawable;
        if (mIsPlayingMusic) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(mContext, R.drawable.avd_play_to_pause);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(mContext, R.drawable.avd_pause_to_play);
        }

        mPlayPauseButton.setImageDrawable(drawable);
        drawable.start();
    }

    private void updateShuffleButton() {
                if (mShuffleEnabled) {
                    mShuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
                } else {
                    mShuffleButton.setImageResource(R.drawable.ic_shuffle);
                }

                mShuffleEnabled = !mShuffleEnabled;

                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .edit()
                        .putBoolean(NowPlayingActivity.SHUFFLE_ENABLED, mShuffleEnabled)
                        .apply();
    }

    private void updateRepeatButton() {
        if (mRepeatMode == NowPlayingActivity.MODE_DISABLED) {
            mRepeatButton.setImageResource(R.drawable.ic_repeat);
            mRepeatMode = NowPlayingActivity.MODE_ENABLED;
        } else if (mRepeatMode == NowPlayingActivity.MODE_ENABLED) {
            mRepeatButton.setImageResource(R.drawable.ic_repeat_song);
            mRepeatMode = NowPlayingActivity.MODE_SINGLE;
        } else {
            mRepeatButton.setImageResource(R.drawable.ic_repeat_disabled);
            mRepeatMode = NowPlayingActivity.MODE_DISABLED;
        }

        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putInt(NowPlayingActivity.REPEAT_MODE, mRepeatMode)
                .apply();
    }

    private void checkRepeatMode() {
        if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
            mRepeatMode = NowPlayingActivity.MODE_ENABLED;
            mRepeatButton.setImageResource(R.drawable.ic_repeat);

            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putInt(NowPlayingActivity.REPEAT_MODE, mRepeatMode)
                    .apply();
        }
    }
}
