package me.mikecasper.musicvoice.services.musicplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.mikecasper.musicvoice.MainActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.responses.TrackResponseItem;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.services.AudioBroadcastReceiver;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.PauseMusicEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SetPlaylistEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.StopSeekbarUpdateEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.util.Logger;

public class MusicPlayer extends Service implements ConnectionStateCallback, PlayerNotificationCallback, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "MusicPlayer";
    private static final int NOTIFICATION_ID = 1;

    // Intent Actions
    public static final String CREATE_PLAYER = "me.mikecasper.musicvoice.MusicPlayer.CREATE_PLAYER";
    public static final String PAUSE_PLAYER = "me.mikecasper.musicvoice.MusicPlayer.PAUSE_PLAYER";
    public static final String RESUME_PLAYER = "me.mikecasper.musicvoice.MusicPlayer.RESUME_PLAYER";
    public static final String SKIP_FORWARD_PLAYER = "me.mikecasper.musicvoice.MusicPlayer.SKIP_FORWARD_PLAYER";
    public static final String SKIP_BACKWARD_PLAYER = "me.mikecasper.musicvoice.MusicPlayer.SKIP_BACKWARD_PLAYER";
    public static final String CLOSE_PLAYER = "me.mikecasper.musicvoice.MusicPlayer.DESTROY_PLAYER";

    private Player mPlayer;
    private boolean mShuffleEnabled;
    private boolean mShuffleWasEnabled;
    private boolean mIsPlaying;
    private boolean mHasFocus;
    private int mRepeatMode;
    private int mSongIndex;
    private int mPlaylistSize;
    private List<Pair<Track, Integer>> mTracks;
    private List<Track> mOriginalTracks;
    private IEventManager mEventManager;
    private AudioManager mAudioManager;
    private BroadcastReceiver mAudioBroadcastReceiver;
    private final IntentFilter mIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NotificationCompat.Builder mNotificationBuilder;

    // Song time stuff
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private ScheduledFuture<?> mScheduledFuture;

    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();
    private long mPreviousTime;
    private int mPreviousSongTime;
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mEventManager = EventManagerProvider.getInstance(this);
        mEventManager.register(this);

        mTracks = new ArrayList<>();
        mOriginalTracks = new ArrayList<>();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        handleIntent(intent);

        return super.onStartCommand(intent, flags, startId);
        //return START_STICKY; TODO not sure about this
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        switch (action) {
            case CREATE_PLAYER:
                initPlayer();
                break;
            case RESUME_PLAYER:
                break;
            case PAUSE_PLAYER:
                break;
            case SKIP_FORWARD_PLAYER:
                break;
            case SKIP_BACKWARD_PLAYER:
                break;
            case CLOSE_PLAYER:
                onDestroy();
                break;
        }
    }

    private void initPlayer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString(LogInService.SPOTIFY_TOKEN, null);

        Config playerConfig = new Config(this, token, LogInService.CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                player.addConnectionStateCallback(MusicPlayer.this);
                player.addPlayerNotificationCallback(MusicPlayer.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.e(TAG, "Could not initialize player", throwable);
            }
        });

        mRepeatMode = sharedPreferences.getInt(NowPlayingActivity.REPEAT_MODE, NowPlayingActivity.MODE_DISABLED);
        mShuffleEnabled = sharedPreferences.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mEventManager.unregister(this);
        if (mIsPlaying) {
            mEventManager.postEvent(new UpdatePlayerStatusEvent(false, mTracks.get(mSongIndex).first));
            mPlayer.pause();
            mIsPlaying = false;
        }
        abandonFocus();
        Spotify.destroyPlayer(this);
        mPlayer = null;
        stopSeekBarUpdate();
        stopForeground(true);
        mExecutorService.shutdown();
    }

    @Subscribe
    public void onPauseMusic(PauseMusicEvent event) {
        pauseMusic();
        // TODO update player status
        // TODO maybe do update inside the pause music method?
    }

    @Subscribe
    public void onToggleShuffle(ToggleShuffleEvent event) {
        mShuffleWasEnabled = mShuffleEnabled;
        mShuffleEnabled = !mShuffleEnabled;

        organizeTracks(false, -1);
    }

    @Subscribe
    public void onToggleRepeat(ToggleRepeatEvent event) {
        mRepeatMode = ++mRepeatMode % 3;
    }

    @Subscribe
    public void onSetPlaylist(SetPlaylistEvent event) {
        List<TrackResponseItem> items = event.getTracks();

        mPlaylistSize = items.size();
        mOriginalTracks.clear();

        for (TrackResponseItem item : items) {
            mOriginalTracks.add(item.getTrack());
        }

        organizeTracks(true, event.getPosition());

        if (requestFocus()) {
            mHasFocus = true;

            if (mPlayer == null) {
                initPlayer();
            }

            mPlayer.play(mTracks.get(0).first.getUri());
            mIsPlaying = true;

            mPreviousSongTime = 0;

            mPreviousTime = SystemClock.elapsedRealtime();
            scheduleSeekBarUpdate();

            setAsForgroundService();
            registerReceiver(mAudioBroadcastReceiver, mIntentFilter);
        }
    }

    private void organizeTracks(boolean refreshTracks, int position) {
        Pair<Track, Integer> firstTrack;

        if ((mShuffleWasEnabled && !mShuffleEnabled) || refreshTracks) {
            int index = position;

            if (!refreshTracks) {
                index = mTracks.get(mSongIndex).second;
            }

            mTracks.clear();

            for (int i = index; i < mOriginalTracks.size(); i++) {
                mTracks.add(new Pair<>(mOriginalTracks.get(i), i));
            }

            for (int i = 0; i < index; i++) {
                mTracks.add(new Pair<>(mOriginalTracks.get(i), i));
            }

            firstTrack = mTracks.remove(0);
        } else {
            firstTrack = mTracks.remove(mSongIndex);
        }

        if (mShuffleEnabled) {
            Collections.shuffle(mTracks);
        }

        mTracks.add(0, firstTrack);
        mSongIndex = 0;
    }

    @Subscribe
    public void onGetPlayerStatus(GetPlayerStatusEvent event) {
        Track track = null;

        if (!mTracks.isEmpty()) {
            track = mTracks.get(mSongIndex).first;
        }
        mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, track));
    }

    @Subscribe
    public void onTogglePlayback(TogglePlaybackEvent event) {
        if (mPlayer == null) {
            initPlayer();
        }

        if (mIsPlaying) {
            pauseMusic();
        } else {
            if (mHasFocus || requestFocus()) {
                mHasFocus = true;
                mPlayer.resume();
                mPreviousTime = SystemClock.elapsedRealtime();
                scheduleSeekBarUpdate();

                mIsPlaying = true;
            }
        }
    }

    private void playMusic() {

    }

    private void pauseMusic() {
        mPlayer.pause();
        stopSeekBarUpdate();

        mIsPlaying = false;

        unregisterReceiver(mAudioBroadcastReceiver);
    }

    @Subscribe
    public void onSeekTo(SeekToEvent event) {
        if (mPlayer == null) {
            initPlayer();
        }

        mPlayer.seekToPosition(event.getPosition());
        mPreviousSongTime = event.getPosition();
        scheduleSeekBarUpdate();
    }

    @Subscribe
    public void onSkipForward(SkipForwardEvent event) {
        mIsPlaying = true;

        if (mPlayer == null) {
            initPlayer();
        }

        if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
            mRepeatMode = NowPlayingActivity.MODE_ENABLED;
        }

        playNextSong();
    }

    @Subscribe
    public void onSkipBackward(SkipBackwardEvent event) {
        mIsPlaying = true;

        if (mPlayer == null) {
            initPlayer();
        }

        if (mRepeatMode == NowPlayingActivity.MODE_SINGLE) {
            mRepeatMode = NowPlayingActivity.MODE_ENABLED;
        }

        playPreviousSong();
    }

    @Subscribe
    public void onStopSeekUpdates(StopSeekbarUpdateEvent event) {
        stopSeekBarUpdate();
    }

    private void playNextSong() {
        boolean shouldPlaySong = true;

        if (mRepeatMode != NowPlayingActivity.MODE_SINGLE) {
            mSongIndex = ++mSongIndex % mPlaylistSize;

            if ((!mShuffleEnabled && mTracks.get(mSongIndex).second == 0) || (mShuffleEnabled && mSongIndex == 0)) {
                if (mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                    shouldPlaySong = false;
                }
            }
        }

        mPlayer.play(mTracks.get(mSongIndex).first.getUri());

        if (!shouldPlaySong) {
            mPlayer.pause();
            mIsPlaying = false;
        }

        mEventManager.postEvent(new SongChangeEvent(mTracks.get(mSongIndex).first, shouldPlaySong));
        updateTask();
    }

    private void playPreviousSong() {
        boolean shouldPlaySong = true;

        --mSongIndex;

        if (mSongIndex == -1) {
            mSongIndex = mPlaylistSize - 1;

            if (mShuffleEnabled && mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                shouldPlaySong = false;
            }
        }

        if (mTracks.get(mSongIndex).second == -1 && mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
            shouldPlaySong = false;
        }

        mPlayer.play(mTracks.get(mSongIndex).first.getUri());

        if (!shouldPlaySong) {
            mPlayer.pause();
            mIsPlaying = false;
        }

        mEventManager.postEvent(new SongChangeEvent(mTracks.get(mSongIndex).first, shouldPlaySong));
        updateTask();
    }

    private void updateTask() {
        mPreviousSongTime = 0;

        if (mIsPlaying) {
            mPreviousTime = SystemClock.elapsedRealtime();
            scheduleSeekBarUpdate();
        } else {
            stopSeekBarUpdate();
        }
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

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Logger.i(TAG, eventType.name());

        switch (eventType) {
            case END_OF_CONTEXT:
                playNextSong();
                break;
            case LOST_PERMISSION:
                stopSeekBarUpdate();
                mIsPlaying = false;
                mEventManager.postEvent(new LostPermissionEvent());
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    // Time stuff
    private void scheduleSeekBarUpdate() {
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduledFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS
            );
        }
    }

    private void stopSeekBarUpdate() {
        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (!mIsPlaying) {
            return;
        }
        long currentTime = SystemClock.elapsedRealtime();

        long difference = currentTime - mPreviousTime;
        mPreviousTime = currentTime;
        mPreviousSongTime += difference;

        mEventManager.postEvent(new UpdateSongTimeEvent(mPreviousSongTime));
    }

    private boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private boolean abandonFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Logger.i("Testing", "Here");
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!mIsPlaying) {
                    mPlayer.resume();
                    mIsPlaying = true;
                    mPreviousTime = SystemClock.elapsedRealtime();
                    scheduleSeekBarUpdate();
                    mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, mTracks.get(mSongIndex).first));
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mIsPlaying) {
                    mPlayer.pause();
                    mIsPlaying = false;
                    stopSeekBarUpdate();
                    mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, mTracks.get(mSongIndex).first));
                }
                abandonFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mIsPlaying) {
                    mPlayer.pause();
                    mIsPlaying = false;
                    stopSeekBarUpdate();
                    mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, mTracks.get(mSongIndex).first));
                }
                break;
        }
    }

    private void setAsForgroundService() {
        Intent intent = new Intent(getApplicationContext(), NowPlayingActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_play)//TODO headphones)
                .setContentTitle("Song Name")
                .setContentText("Artist Names")
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle();

        mNotificationBuilder.setStyle(style);

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private final IBinder mBinder = new MusicPlayerBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MusicPlayerBinder extends Binder {
        MusicPlayer getService() {
            return MusicPlayer.this;
        }
    }
}
