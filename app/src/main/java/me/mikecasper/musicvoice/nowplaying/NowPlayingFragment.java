package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

public class NowPlayingFragment extends Fragment {

    // Constants for Saving View State
    private static final String IS_PLAYING = "isPlaying";

    // Services
    private IEventManager mEventManager;

    // Data Members
    private boolean mIsPlayingMusic;
    private boolean mShuffleEnabled;
    private int mRepeatMode;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private ImageView mAlbumArt;

    // Target for better image loading
    private final Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mAlbumArt.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mAlbumArt.setImageDrawable(errorDrawable);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            // do nothing
        }
    };

    public NowPlayingFragment() {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_now_playing, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventManager = EventManagerProvider.getInstance(getContext());

        Bundle args = getArguments();
        Track track = args.getParcelable(NowPlayingActivity.TRACK);

        boolean shouldPlaySong = args.getBoolean(NowPlayingActivity.SHOULD_PLAY_TRACK, false);
        mIsPlayingMusic = args.getBoolean(NowPlayingActivity.IS_PLAYING_MUSIC, false);

        if (savedInstanceState == null) {
            if (shouldPlaySong) {
                mIsPlayingMusic = true;
            }
        } else {
            mIsPlayingMusic = savedInstanceState.getBoolean(IS_PLAYING);
            mShuffleEnabled = savedInstanceState.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED);
            mRepeatMode = savedInstanceState.getInt(NowPlayingActivity.REPEAT_MODE);
        }

        TextView playlistName = (TextView) view.findViewById(R.id.playlist_name);
        playlistName.setSelected(true);
        playlistName.setSingleLine(true);

        String playlist = args.getString(NowPlayingActivity.PLAYLIST_NAME, null);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (playlist == null) {
            playlist = sharedPreferences.getString(NowPlayingActivity.PLAYLIST_NAME, null);
        } else {
            sharedPreferences.edit()
                    .putString(NowPlayingActivity.PLAYLIST_NAME, playlist)
                    .apply();
        }

        playlistName.setText(getString(R.string.playing_from, playlist));

        setUpButtons();

        mAlbumArt = (ImageView) view.findViewById(R.id.album_art);

        mCurrentTime = (TextView) view.findViewById(R.id.current_time);
        mSeekBar = (SeekBar) view.findViewById(R.id.song_seek_bar);
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

        int currentTime = args.getInt(NowPlayingActivity.CURRENT_TIME, 0);
        mSeekBar.setProgress(currentTime);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_PLAYING, mIsPlayingMusic);
        outState.putBoolean(NowPlayingActivity.SHUFFLE_ENABLED, mShuffleEnabled);
        outState.putInt(NowPlayingActivity.REPEAT_MODE, mRepeatMode);
    }

    private void updateCurrentTime(int position) {
        String time = DateUtility.formatDuration(position);
        mCurrentTime.setText(time);
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpButtons() {
        View view = getView();

        ImageView playPauseButton = (ImageView) view.findViewById(R.id.play_pause_button);

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

        View queueButton = view.findViewById(R.id.queue_button);
        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQueueFragment();
            }
        });
    }

    private void startQueueFragment() {
        Fragment fragment = new QueueFragment();
        Bundle args = getArguments();
        fragment.setArguments(args);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.now_playing_content, fragment)
                .addToBackStack(null) // TODO remove?
                .commit();
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

    @Subscribe
    public void onPlayerStatusUpdated(UpdatePlayerStatusEvent event) {
        boolean wasPlaying = mIsPlayingMusic;
        mIsPlayingMusic = event.isPlaying();

        if (wasPlaying != mIsPlayingMusic) {
            updatePlayButton();
        }

        Track track = event.getTrack();

        if (track != null) {
            Bundle args = getArguments();
            args.putParcelable(NowPlayingActivity.TRACK, track);

            updateView(track);
        } else {
            getActivity().onBackPressed();
        }
    }

    @Subscribe
    public void onSongChange(SongChangeEvent event) {
        Track track = event.getTrack();

        Bundle args = getArguments();
        args.putParcelable(NowPlayingActivity.TRACK, track);

        boolean wasPreviouslyPlaying = mIsPlayingMusic;
        mIsPlayingMusic = event.isPlayingSong();

        if (wasPreviouslyPlaying != mIsPlayingMusic) {
            updatePlayButton();
        }

        View view = getView();

        if (view != null) {
            TextView trackName = (TextView) view.findViewById(R.id.track_name);
            if (trackName != null && !trackName.getText().toString().equals(track.getName())) {

                TextView currentTime = (TextView) view.findViewById(R.id.current_time);
                if (currentTime != null) {
                    currentTime.setText(R.string.initial_time);
                }

                updateView(track);
            }
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

        View view = getView();

        if (view != null) {
            TextView trackName = (TextView) view.findViewById(R.id.track_name);
            TextView artistName = (TextView) view.findViewById(R.id.artist_name);
            TextView remainingTime = (TextView) view.findViewById(R.id.remaining_time);

            Picasso.with(getContext())
                    .load(track.getAlbum().getImages().get(0).getUrl())
                    .error(R.drawable.default_playlist)
                    .into(mTarget);

            if (trackName != null && artistName != null && remainingTime != null) {
                trackName.setText(track.getName());
                trackName.setSelected(true);
                trackName.setSingleLine(true);

                remainingTime.setText(DateUtility.formatDuration(track.getDuration()));

                String artistNames = "";

                for (Artist artist : track.getArtists()) {
                    artistNames += artist.getName() + ", ";
                }

                artistNames = artistNames.substring(0, artistNames.length() - 2);

                artistName.setText(artistNames);
                artistName.setSelected(true);
                artistName.setSingleLine(true);
            }
        }
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

        View view = getView();

        if (view != null) {
            ImageView button = (ImageView) view.findViewById(R.id.play_pause_button);

            if (button != null) {
                button.setImageDrawable(drawable);
                drawable.start();
            }
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
