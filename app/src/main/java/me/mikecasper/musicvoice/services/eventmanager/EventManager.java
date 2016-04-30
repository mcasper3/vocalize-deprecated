package me.mikecasper.musicvoice.services.eventmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;

import java.io.IOException;

import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.api.SpotifyApi;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.events.spotify.SpotifyEvent;
import me.mikecasper.musicvoice.api.services.PlaylistService;
import me.mikecasper.musicvoice.api.services.SpotifyUserService;
import me.mikecasper.musicvoice.util.Logger;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventManager implements IEventManager {

    private static final String TAG = "EventManager";

    private Bus mBus;
    private LogInService mLogInService;
    private SpotifyUserService mSpotifyUserService;
    private PlaylistService mPlaylistService;
    private Context mContext;

    EventManager(Context context) {
        mContext = context;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer " + getSpotifyToken())
                        .build();

                return chain.proceed(request);
            }
        });

        if (MusicVoiceApplication.LOG_LEVEL == MusicVoiceApplication.LogLevel.FULL) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            builder.addInterceptor(loggingInterceptor);
        }

        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        SpotifyApi api = retrofit.create(SpotifyApi.class);

        mBus = BusProvider.getBus();
        mLogInService = new LogInService();
        mSpotifyUserService = new SpotifyUserService(mBus, api);
        mPlaylistService = new PlaylistService(mBus, api);

        subscribeServices();
    }

    private String getSpotifyToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        return sharedPreferences.getString(LogInService.SPOTIFY_TOKEN, "Empty");
    }

    @Override
    public void register(Object object) {
        try {
            mBus.register(object);
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, "Error subscribing object", e);
        }
    }

    @Override
    public void unregister(Object object) {
        try {
            mBus.unregister(object);
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, "Error un-subscribing object", e);
        }
    }

    @Override
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
        mBus.register(mPlaylistService);
    }
}
