package me.mikecasper.musicvoice.overview;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    private Context mContext;

    public SpotifyUserService(Bus bus, SpotifyApi api, Context context) {
        this.mBus = bus;
        this.mApi = api;
        this.mContext = context;
    }

    @Subscribe
    public void onGetUser(GetUserRequest request) {
        Call<SpotifyUser> call = mApi.getUserInfo();

        call.enqueue(new Callback<SpotifyUser>() {
            @Override
            public void onResponse(Call<SpotifyUser> call, Response<SpotifyUser> response) {
                SpotifyUser user = response.body();
                if (user != null) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SpotifyUser.NAME, user.getDisplay_name());

                    if (user.getImages().length > 0)
                        editor.putString(SpotifyUser.PROFILE_IMAGE, user.getImages()[0].getUrl());

                    editor.apply();

                    mBus.post(user);
                }
            }

            @Override
            public void onFailure(Call<SpotifyUser> call, Throwable t) {

            }
        });
    }
}
