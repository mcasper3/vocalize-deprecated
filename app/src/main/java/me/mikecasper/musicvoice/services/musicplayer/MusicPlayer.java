package me.mikecasper.musicvoice.services.musicplayer;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.services.musicplayer.events.PlaySongEvent;
import me.mikecasper.musicvoice.util.Logger;

public class MusicPlayer implements ConnectionStateCallback {

    private static final String TAG = "MusicPlayer";
    private Player mPlayer;

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
    public void onPlaySongEvent(PlaySongEvent event) {
        mPlayer.play(event.getUri());
    }

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
