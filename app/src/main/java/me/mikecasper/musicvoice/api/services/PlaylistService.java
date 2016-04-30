package me.mikecasper.musicvoice.api.services;

import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.api.SpotifyApi;
import me.mikecasper.musicvoice.api.responses.TrackResponse;
import me.mikecasper.musicvoice.playlist.events.GetPlaylistTracksEvent;
import me.mikecasper.musicvoice.playlist.events.GetPlaylistsEvent;
import me.mikecasper.musicvoice.api.responses.PlaylistResponse;
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
    public void onGetPlaylists(GetPlaylistsEvent request) {
        Call<PlaylistResponse> call = mApi.getUserPlaylists();

        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                PlaylistResponse playlistResponse = response.body();

                if (playlistResponse != null) {
                    mBus.post(playlistResponse);
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {

            }
        });
    }

    @Subscribe
    public void onGetPlaylistTracks(GetPlaylistTracksEvent event) {
        Call<TrackResponse> call = mApi.getPlaylistTracks(event.getUserId(), event.getPlaylistId(), event.getOffset());

        call.enqueue(new Callback<TrackResponse>() {
            @Override
            public void onResponse(Call<TrackResponse> call, Response<TrackResponse> response) {
                TrackResponse trackResponse = response.body();

                if (trackResponse != null) {
                    mBus.post(trackResponse);
                }
            }

            @Override
            public void onFailure(Call<TrackResponse> call, Throwable t) {

            }
        });
    }
}
