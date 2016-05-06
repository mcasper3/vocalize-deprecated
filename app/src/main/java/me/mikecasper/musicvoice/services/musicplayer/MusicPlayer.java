package me.mikecasper.musicvoice.services.musicplayer;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.mikecasper.musicvoice.api.responses.TrackResponseItem;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.services.musicplayer.events.PlaySongEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SetPlaylistEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.util.Logger;

public class MusicPlayer implements ConnectionStateCallback {

    private static final String TAG = "MusicPlayer";
    private Player mPlayer;
    private List<TrackResponseItem> mTracks;
    private List<String> mUris;
    private boolean mShuffleEnabled;
    private int mRepeatMode;

    public MusicPlayer() { }

    @Subscribe
    public void createPlayer(CreatePlayerEvent event) {
        Config playerConfig = new Config(event.getContext(), event.getToken(), LogInService.CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                player.addConnectionStateCallback(MusicPlayer.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.e(TAG, "Could not initialize player", throwable);
            }
        });
    }

    @Subscribe
    public void onToggleShuffle(ToggleShuffleEvent event) {
        mShuffleEnabled = true;
    }

    @Subscribe
    public void onPlaySongEvent(PlaySongEvent event) {
        // TODO rework this method
        mPlayer.play(event.getUri());
    }

    @Subscribe
    public void onSetPlaylist(SetPlaylistEvent event) {
        List<TrackResponseItem> temp = event.getTracks();

        mTracks = new ArrayList<>(temp.subList(event.getPosition(), temp.size()));
        mTracks.addAll(new ArrayList<>(temp.subList(0, event.getPosition())));

        mUris = new ArrayList<>();

        for (TrackResponseItem track : mTracks) {
            mUris.add(track.getTrack().getUri());
        }

        String firstUri = mUris.remove(0);

        if (mShuffleEnabled) {
            Collections.shuffle(mUris);
        }

        mUris.add(0, firstUri);
        mPlayer.play(mUris);
    }

    @Subscribe
    public void onSkipForward(SkipForwardEvent event) {
        mPlayer.skipToNext();
    }

    @Subscribe
    public void onSkipBackward(SkipBackwardEvent event) {
        mPlayer.skipToPrevious();
    }

    // ConnectionStateCallback Methods
    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }
}
