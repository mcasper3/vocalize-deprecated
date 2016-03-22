package me.mikecasper.musicvoice.overview;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.api.SpotifyApi;
import me.mikecasper.musicvoice.api.requests.GetPlaylistsRequest;
import me.mikecasper.musicvoice.api.responses.PlaylistResponse;
import me.mikecasper.musicvoice.overview.events.PlaylistsObtainedEvent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistService {

    private Bus mBus;
    private SpotifyApi mApi;

    public PlaylistService(Bus mBus, SpotifyApi mApi) {
        this.mBus = mBus;
        this.mApi = mApi;
    }

    @Subscribe
    public void onGetPlaylists(GetPlaylistsRequest request) {
        Call<PlaylistResponse> call = mApi.getUserPlaylists();

        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                PlaylistResponse playlistResponse = response.body();

                if (playlistResponse != null) {
                    mBus.post(new PlaylistsObtainedEvent(playlistResponse.getPlaylists()));
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {

            }
        });
    }
}
