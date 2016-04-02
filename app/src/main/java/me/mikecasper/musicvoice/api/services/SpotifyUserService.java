package me.mikecasper.musicvoice.api.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.api.SpotifyApi;
import me.mikecasper.musicvoice.login.events.GetUserEvent;
import me.mikecasper.musicvoice.models.SpotifyUser;
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
                SpotifyUser user = response.body();
                if (user != null) {
                    mBus.post(user);
                }
            }

            @Override
            public void onFailure(Call<SpotifyUser> call, Throwable t) {

            }
        });
    }
}
