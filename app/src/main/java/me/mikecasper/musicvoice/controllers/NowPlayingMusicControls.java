package me.mikecasper.musicvoice.controllers;

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

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;

public class NowPlayingMusicControls implements MusicButtonsController {

    private View mView;
    private Context mContext;
    private ImageView mPlayPauseButton;
    private boolean mIsPlayingMusic;
    private boolean mShuffleEnabled;
    private int mRepeatMode;

    public NowPlayingMusicControls(View view) {
        mContext = mView.getContext();


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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mRepeatMode = preferences.getInt(NowPlayingActivity.REPEAT_MODE, NowPlayingActivity.MODE_DISABLED);
        mShuffleEnabled = preferences.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED, false);

        ImageView repeatButton = (ImageView) view.findViewById(R.id.repeat_button);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new ToggleRepeatEvent());
                updateRepeatButton();
            }
        });

        if (mRepeatMode == NowPlayingActivity.MODE_ENABLED) {
            repeatButton.setImageResource(R.drawable.ic_repeat);
        } else if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
            repeatButton.setImageResource(R.drawable.ic_repeat_song);
        }

        ImageView shuffleButton = (ImageView) view.findViewById(R.id.shuffle_button);
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
        // TODO
    }

    private void updatePlayButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton();
        } else {
            int id = mIsPlayingMusic ? R.drawable.ic_pause : R.drawable.ic_play;

            View view = getView();

            if (view != null) {
                ImageView button = (ImageView) view.findViewById(R.id.play_pause_button);

                if (button != null) {
                    button.setImageResource(id);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton() {
        AnimatedVectorDrawable drawable;
        if (mIsPlayingMusic) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_play_to_pause);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_pause_to_play);
        }

        mPlayPauseButton.setImageDrawable(drawable);
        drawable.start();
    }

    private void updateShuffleButton() {
        View view = getView();

        if (view != null) {
            ImageView shuffleButton = (ImageView) view.findViewById(R.id.shuffle_button);

            if (shuffleButton != null) {
                if (mShuffleEnabled) {
                    shuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
                } else {
                    shuffleButton.setImageResource(R.drawable.ic_shuffle);
                }

                mShuffleEnabled = !mShuffleEnabled;

                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putBoolean(NowPlayingActivity.SHUFFLE_ENABLED, mShuffleEnabled)
                        .apply();
            }
        }
    }

    private void updateRepeatButton() {
        View view = getView();

        if (view != null) {
            ImageView repeatButton = (ImageView) view.findViewById(R.id.repeat_button);

            if (repeatButton != null) {
                if (mRepeatMode == NowPlayingActivity.MODE_DISABLED) {
                    repeatButton.setImageResource(R.drawable.ic_repeat);
                    mRepeatMode = NowPlayingActivity.MODE_ENABLED;
                } else if (mRepeatMode == NowPlayingActivity.MODE_ENABLED) {
                    repeatButton.setImageResource(R.drawable.ic_repeat_song);
                    mRepeatMode = NowPlayingActivity.MODE_SINGLE;
                } else {
                    repeatButton.setImageResource(R.drawable.ic_repeat_disabled);
                    mRepeatMode = NowPlayingActivity.MODE_DISABLED;
                }

                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putInt(NowPlayingActivity.REPEAT_MODE, mRepeatMode)
                        .apply();
            }
        }
    }

    private void checkRepeatMode() {
        View view = getView();

        if (view != null) {
            ImageView repeatButton = (ImageView) view.findViewById(R.id.repeat_button);

            if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
                mRepeatMode = NowPlayingActivity.MODE_ENABLED;
                repeatButton.setImageResource(R.drawable.ic_repeat);

                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putInt(NowPlayingActivity.REPEAT_MODE, mRepeatMode)
                        .apply();
            }
        }
    }
}
