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

        List<String> uris = getUris();

        mPlayer.play(uris);
    }

    private List<String> getUris() {
        List<String> uris = new ArrayList<>();

        for (TrackResponseItem track : mTracks) {
            uris.add(track.getTrack().getUri());
        }

        String firstUri = uris.remove(0);

        if (mShuffleEnabled) {
            Collections.shuffle(uris);
        }

        uris.add(0, firstUri);

        return uris;
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
}
