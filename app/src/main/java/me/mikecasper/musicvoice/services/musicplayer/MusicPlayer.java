package me.mikecasper.musicvoice.services.musicplayer;

import android.os.Handler;
import android.os.SystemClock;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.mikecasper.musicvoice.api.responses.TrackResponseItem;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.CreatePlayerEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.DestroyPlayerEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.PlaySongEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SetPlaylistEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.StopSeekbarUpdateEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.util.Logger;

public class MusicPlayer implements ConnectionStateCallback, PlayerNotificationCallback {

    private static final String TAG = "MusicPlayer";
    private Player mPlayer;
    private boolean mShuffleEnabled;
    private boolean mShuffleWasEnabled;
    private boolean mIsPlaying;
    private int mRepeatMode;
    private int mSongIndex;
    private int mActualIndex;
    private int mPlaylistSize;
    private List<Track> mTracks;
    private List<Track> mOriginalTracks;
    private IEventManager mEventManager;

    // Song time stuff
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

    public MusicPlayer(IEventManager eventManager) {
        mEventManager = eventManager;
        mTracks = new ArrayList<>();
        mOriginalTracks = new ArrayList<>();
    }

    @Subscribe
    public void createPlayer(CreatePlayerEvent event) {
        Config playerConfig = new Config(event.getContext(), event.getToken(), LogInService.CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                player.addConnectionStateCallback(MusicPlayer.this);
                player.addPlayerNotificationCallback(MusicPlayer.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.e(TAG, "Could not initialize player", throwable);
            }
        });

        mRepeatMode = event.getRepeatMode();
        mShuffleEnabled = event.isShuffleEnabled();
    }

    @Subscribe
    public void onToggleShuffle(ToggleShuffleEvent event) {
        mShuffleWasEnabled = mShuffleEnabled;
        mShuffleEnabled = !mShuffleEnabled;

        organizeTracks(false);
    }

    @Subscribe
    public void onToggleRepeat(ToggleRepeatEvent event) {
        mRepeatMode = ++mRepeatMode % 3;
    }

    @Subscribe
    public void onPlaySongEvent(PlaySongEvent event) {
        mPlayer.play(mTracks.get(0).getUri());
        mIsPlaying = true;

        mPreviousSongTime = 0;

        mPreviousTime = SystemClock.elapsedRealtime();
        scheduleSeekBarUpdate();
    }

    @Subscribe
    public void onSetPlaylist(SetPlaylistEvent event) {
        List<TrackResponseItem> items = event.getTracks();

        mPlaylistSize = items.size();
        mActualIndex = event.getPosition();
        mOriginalTracks.clear();

        for (TrackResponseItem item : items) {
            mOriginalTracks.add(item.getTrack());
        }

        organizeTracks(true);
    }

    private void organizeTracks(boolean refreshTracks) {
        Track firstTrack;

        if ((mShuffleWasEnabled && !mShuffleEnabled) || refreshTracks) {
            mTracks.clear();

            mTracks.addAll(mOriginalTracks.subList(mActualIndex, mOriginalTracks.size()));
            mTracks.addAll(mOriginalTracks.subList(0, mActualIndex));

            firstTrack = mTracks.remove(0);
        } else {
            firstTrack = mTracks.remove(mSongIndex);
        }

        if (mShuffleEnabled) {
            Collections.shuffle(mTracks);
        }

        mTracks.add(0, firstTrack);
        mSongIndex = 0;
    }

    @Subscribe
    public void onGetPlayerStatus(GetPlayerStatusEvent event) {
        Track track = null;

        if (!mTracks.isEmpty()) {
            track = mTracks.get(mSongIndex);
        }
        mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, track));
    }

    @Subscribe
    public void onTogglePlayback(TogglePlaybackEvent event) {
        if (mIsPlaying) {
            mPlayer.pause();
            stopSeekBarUpdate();
        } else {
            mPlayer.resume();
            mPreviousTime = SystemClock.elapsedRealtime();
            scheduleSeekBarUpdate();
        }

        mIsPlaying = !mIsPlaying;
    }

    @Subscribe
    public void onSeekTo(SeekToEvent event) {
        mPlayer.seekToPosition(event.getPosition());
        mPreviousSongTime = event.getPosition();
        scheduleSeekBarUpdate();
    }

    @Subscribe
    public void onSkipForward(SkipForwardEvent event) {
        mIsPlaying = true;

        if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
            mRepeatMode = NowPlayingActivity.MODE_ENABLED;
        }

        playNextSong();
    }

    @Subscribe
    public void onSkipBackward(SkipBackwardEvent event) {
        mIsPlaying = true;

        if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
            mRepeatMode = NowPlayingActivity.MODE_ENABLED;
        }

        playPreviousSong();
    }

    @Subscribe
    public void onStopSeekUpdates(StopSeekbarUpdateEvent event) {
        stopSeekBarUpdate();
    }

    private void playNextSong() {
        boolean shouldPlaySong = true;

        if (mRepeatMode != NowPlayingActivity.MODE_SINGLE) {
            mSongIndex = ++mSongIndex % mPlaylistSize;
            mActualIndex = ++mActualIndex % mPlaylistSize;

            if ((!mShuffleEnabled && mActualIndex == 0) || (mShuffleEnabled && mSongIndex == 0)) {
                if (mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                    shouldPlaySong = false;
                }
            }
        }

        mPlayer.play(mTracks.get(mSongIndex).getUri());

        if (!shouldPlaySong) {
            mPlayer.pause();
            mIsPlaying = false;
        }

        mEventManager.postEvent(new SongChangeEvent(mTracks.get(mSongIndex), shouldPlaySong));
        updateTask();
    }

    private void playPreviousSong() {
        boolean shouldPlaySong = true;

        --mSongIndex;
        --mActualIndex;

        if (mSongIndex == -1) {
            mSongIndex = mPlaylistSize - 1;

            if (mShuffleEnabled && mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                shouldPlaySong = false;
            }
        }

        if (mActualIndex == -1) {
            mActualIndex = mPlaylistSize - 1;

            if (mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                shouldPlaySong = false;
            }
        }

        mPlayer.play(mTracks.get(mSongIndex).getUri());

        if (!shouldPlaySong) {
            mPlayer.pause();
            mIsPlaying = false;
        }

        mEventManager.postEvent(new SongChangeEvent(mTracks.get(mSongIndex), shouldPlaySong));
        updateTask();
    }

    private void updateTask() {
        mPreviousSongTime = 0;

        if (mIsPlaying) {
            mPreviousTime = SystemClock.elapsedRealtime();
            scheduleSeekBarUpdate();
        } else {
            stopSeekBarUpdate();
        }
    }

    // ConnectionStateCallback Methods
    @Override
    public void onLoggedIn() {
        Logger.d(TAG, "Logged in");
    }

    @Override
    public void onLoggedOut() {
        Logger.d(TAG, "Logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Logger.e(TAG, "Login failed", throwable);
    }

    @Override
    public void onTemporaryError() {
        Logger.d(TAG, "Temporary error");
    }

    @Override
    public void onConnectionMessage(String s) {
        Logger.d(TAG, s);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Logger.i(TAG, eventType.name());

        switch (eventType) {
            case END_OF_CONTEXT:
                playNextSong();
                break;
            case LOST_PERMISSION:
                stopSeekBarUpdate();
                mIsPlaying = false;
                mEventManager.postEvent(new LostPermissionEvent());
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    // Time stuff
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
        if (!mIsPlaying) {
            return;
        }
        long currentTime = SystemClock.elapsedRealtime();

        long difference = currentTime - mPreviousTime;
        mPreviousTime = currentTime;
        mPreviousSongTime += difference;

        mEventManager.postEvent(new UpdateSongTimeEvent(mPreviousSongTime));
    }

    @Subscribe
    public void onDestroyPlayer(DestroyPlayerEvent event) {
        if (mIsPlaying) {
            mPlayer.pause();
            mIsPlaying = false;
        }
        Spotify.destroyPlayer(this);
        stopSeekBarUpdate();
        mExecutorService.shutdown();
    }
}
