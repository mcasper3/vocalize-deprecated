package me.mikecasper.vocalize.api.services;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import me.mikecasper.vocalize.api.SpotifyApi;
import me.mikecasper.vocalize.login.events.GetUserEvent;
import me.mikecasper.vocalize.login.events.RefreshTokenEvent;
import me.mikecasper.vocalize.models.SpotifyUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpotifyUserService {

    private Bus mBus;
    private SpotifyApi mApi;

    public SpotifyUserService(Bus bus, SpotifyApi api) {
        this.mBus = bus;
        this.mApi = api;
    }

    @Subscribe
    public void onGetUser(GetUserEvent request) {
        Call<SpotifyUser> call = mApi.getUserInfo();

        call.enqueue(new Callback<SpotifyUser>() {
            @Override
            public void onResponse(Call<SpotifyUser> call, Response<SpotifyUser> response) {
                if (response.code() == 401) {
                    mBus.post(new RefreshTokenEvent(call, this));
                } else {
                    SpotifyUser user = response.body();
                    if (user != null) {
                        mBus.post(user);
                    }
                }
            }

            @Override
            public void onFailure(Call<SpotifyUser> call, Throwable t) {

            }
        });
    }
}
