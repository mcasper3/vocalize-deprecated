package me.mikecasper.musicvoice.overview;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.api.SpotifyApi;
import me.mikecasper.musicvoice.api.requests.GetUserRequest;
import me.mikecasper.musicvoice.models.SpotifyUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpotifyUserService {

    private Bus mBus;
    private SpotifyApi mApi;

    public SpotifyUserService(Bus mBus, SpotifyApi mApi) {
        this.mBus = mBus;
        this.mApi = mApi;
    }

    @Subscribe
    public void onGetUser(GetUserRequest request) {
        Call<SpotifyUser> call = mApi.getUserInfo();

        call.enqueue(new Callback<SpotifyUser>() {
            @Override
            public void onResponse(Call<SpotifyUser> call, Response<SpotifyUser> response) {
                if (response.body() != null) {

                }
            }

            @Override
            public void onFailure(Call<SpotifyUser> call, Throwable t) {

            }
        });
    }
}
