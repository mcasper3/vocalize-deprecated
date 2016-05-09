package me.mikecasper.musicvoice.services.musicplayer;

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

import me.mikecasper.musicvoice.api.responses.TrackResponseItem;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.CreatePlayerEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.PlaySongEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SetPlaylistEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
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
        } else {
            mPlayer.resume();
        }

        mIsPlaying = !mIsPlaying;
    }

    @Subscribe
    public void onSeekTo(SeekToEvent event) {
        mPlayer.seekToPosition(event.getPosition());
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
                mIsPlaying = false;
                mEventManager.postEvent(new LostPermissionEvent());
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }
}
