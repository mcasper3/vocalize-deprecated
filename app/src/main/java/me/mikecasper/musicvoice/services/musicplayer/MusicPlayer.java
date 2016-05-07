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
import me.mikecasper.musicvoice.services.musicplayer.events.PlaySongEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SetPlaylistEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.util.Logger;

public class MusicPlayer implements ConnectionStateCallback, PlayerNotificationCallback {

    private static final String TAG = "MusicPlayer";
    private Player mPlayer;
    private List<TrackResponseItem> mTracksItems;
    private boolean mShuffleEnabled;
    private int mRepeatMode;
    private int mSongIndex;
    private int mActualIndex;
    private int mPlaylistSize;
    private List<Track> mTracks;
    private boolean mIsPlaying;
    private IEventManager mEventManager;

    public MusicPlayer(IEventManager eventManager) {
        mEventManager = eventManager;
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
        mShuffleEnabled = !mShuffleEnabled;

        getTracks();
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
        List<TrackResponseItem> temp = event.getTracks();

        mTracksItems = new ArrayList<>(temp.subList(event.getPosition(), temp.size()));
        mTracksItems.addAll(new ArrayList<>(temp.subList(0, event.getPosition())));

        mPlaylistSize = mTracksItems.size();
        mSongIndex = 0;
        mActualIndex = event.getPosition();

        mTracks = getTracks();
    }

    private List<Track> getTracks() {
        List<Track> tracks = new ArrayList<>();

        for (TrackResponseItem track : mTracksItems) {
            tracks.add(track.getTrack());
        }

        Track firstTrack = tracks.remove(0);

        if (mShuffleEnabled) {
            Collections.shuffle(tracks);
        }

        tracks.add(0, firstTrack);

        return tracks;
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
        playNextSong();
        mIsPlaying = true;
    }

    @Subscribe
    public void onSkipBackward(SkipBackwardEvent event) {
        playPreviousSong();
        mIsPlaying = true;
    }

    private void playNextSong() {
        boolean shouldPlaySong = true;

        mSongIndex = ++mSongIndex % mPlaylistSize;
        mActualIndex = ++ mActualIndex % mPlaylistSize;

        if (mActualIndex == 0) {
            if (mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                shouldPlaySong = false;
            }
        }

        if (shouldPlaySong) {
            mPlayer.play(mTracks.get(mSongIndex).getUri());
        } else {
            mPlayer.pause();
        }

        mEventManager.postEvent(new SongChangeEvent(mTracks.get(mSongIndex), shouldPlaySong));
    }

    private void playPreviousSong() {
        boolean shouldPlaySong = true;

        --mSongIndex;
        --mActualIndex;

        if (mActualIndex == -1) {
            mSongIndex = mPlaylistSize - 1;
            mActualIndex = mPlaylistSize - 1;

            if (mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                shouldPlaySong = false;
            }
        }

        if (shouldPlaySong) {
            mPlayer.play(mTracks.get(mSongIndex).getUri());
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

        // TODO account for playback on other devices
        switch (eventType) {
            case END_OF_CONTEXT:
                // TODO
                playNextSong();
                break;
            case LOST_PERMISSION:
                // TODO update view to reflect changes
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }
}
