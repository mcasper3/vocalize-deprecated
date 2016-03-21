package me.mikecasper.musicvoice.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.otto.Bus;

import java.io.IOException;

import me.mikecasper.musicvoice.api.SpotifyApi;
import me.mikecasper.musicvoice.login.LogInService;
import me.mikecasper.musicvoice.events.spotify.SpotifyEvent;
import me.mikecasper.musicvoice.overview.SpotifyUserService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventManager {

    private static final String TAG = "EventManager";

    private Bus mBus;
    private LogInService mLogInService;
    private SpotifyUserService mSpotifyUserService;
    private Context mContext;

    EventManager(Context context) {
        mContext = context;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("Authorization", "Bearer " + getSpotifyToken())
                                .build();

                        return chain.proceed(request);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        SpotifyApi api = retrofit.create(SpotifyApi.class);

        mBus = BusProvider.getBus();
        mLogInService = new LogInService();
        mSpotifyUserService = new SpotifyUserService(mBus, api, mContext);

        subscribeServices();
    }

    private String getSpotifyToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        return sharedPreferences.getString(LogInService.SPOTIFY_TOKEN, "Empty");
    }

    public void register(Object object) {
        try {
            mBus.register(object);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error subscribing object", e);
        }
    }

    public void unregister(Object object) {
        try {
            mBus.unregister(object);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error un-subscribing object", e);
        }
    }

    public void postEvent(Object object) {
        if (object instanceof SpotifyEvent) {
            // todo check for token expiration time and do authentication if needed
            /*if () {

            } else {

            }*/
            mBus.post(object);
        } else {
            mBus.post(object);
        }
    }

    private void subscribeServices() {
        mBus.register(mLogInService);
        mBus.register(mSpotifyUserService);
    }
}