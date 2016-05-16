package me.mikecasper.musicvoice.services.musicplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.responses.TrackResponseItem;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.services.AudioBroadcastReceiver;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.DisplayNotificationEvent;
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

    private static boolean sIsAlive;

    public static boolean isAlive() {
        return sIsAlive;
    }

    private static final String TAG = "MusicPlayer";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 37;

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
    private boolean mIsForeground;
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

    // Intents for notification
    private PendingIntent mPlayIntent;
    private PendingIntent mPauseIntent;
    private PendingIntent mSkipForwardIntent;
    private PendingIntent mSkipBackwardIntent;

    // Target for notification icons
    private Target mTarget;

    // Song time stuff
    private static final long PROGRESS_UPDATE_INTERVAL = 1000;
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

    private static final int NOTIFICATION_DELAY = 250;
    private final Runnable mShowNotification = new Runnable() {
        @Override
        public void run() {
            setAsForegroundService();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.d(TAG, "In on create");

        sIsAlive = true;

        mEventManager = EventManagerProvider.getInstance(this);
        mEventManager.register(this);

        mTracks = new ArrayList<>();
        mOriginalTracks = new ArrayList<>();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                updateIcon(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                // do nothing
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // do nothing
            }
        };

        createIntents();

        initPlayer();

        mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, null));
    }

    private void createIntents() {
        Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);

        intent.setAction(RESUME_PLAYER);
        mPlayIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        intent.setAction(PAUSE_PLAYER);
        mPauseIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        intent.setAction(SKIP_FORWARD_PLAYER);
        mSkipForwardIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        intent.setAction(SKIP_BACKWARD_PLAYER);
        mSkipBackwardIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        handleIntent(intent);

        return START_NOT_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Logger.d(TAG, "Received an intent with action " + intent.getAction());

        String action = intent.getAction();

        switch (action) {
            case CREATE_PLAYER:
                break;
            case RESUME_PLAYER:
                playMusic(true);
                break;
            case PAUSE_PLAYER:
                pauseMusic();
                break;
            case SKIP_FORWARD_PLAYER:
                onSkipForward(null);
                break;
            case SKIP_BACKWARD_PLAYER:
                onSkipBackward(null);
                break;
            case CLOSE_PLAYER:
                stopSelf();
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

        Logger.d(TAG, "In onDestroy");

        sIsAlive = false;

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

        if (mIsForeground) {
            mIsForeground = false;
            stopForeground(true);
        }
        mExecutorService.shutdown();
    }

    @Subscribe
    public void onPauseMusic(PauseMusicEvent event) {
        Logger.d(TAG, "On pause music");

        pauseMusic();
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
        mSongIndex = 0;

        if (mPlayer == null) {
            initPlayer();
        }

        playMusic(false);
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
    public void onDisplayNotification(DisplayNotificationEvent event) {
        mHandler.postDelayed(mShowNotification, NOTIFICATION_DELAY);
    }

    @Subscribe
    public void onGetPlayerStatus(GetPlayerStatusEvent event) {
        Track track = null;

        mHandler.removeCallbacks(mShowNotification);
        stopForeground(true);

        if (!mTracks.isEmpty()) {
            track = mTracks.get(mSongIndex).first;
        }

        mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, track));
    }

    @Subscribe
    public void onTogglePlayback(TogglePlaybackEvent event) {
        Logger.d(TAG, "Toggle playback");

        if (mPlayer == null) {
            initPlayer();
        }

        if (mIsPlaying) {
            pauseMusic();
        } else {
            playMusic(true);
        }
    }

    private void playMusic(boolean shouldResume) {
        Logger.d(TAG, "Playing music");

        if (mHasFocus || requestFocus()) {
            mHasFocus = true;
            mIsPlaying = true;
            registerReceiver(mAudioBroadcastReceiver, mIntentFilter);

            if (mIsForeground) {
                setAsForegroundService();
            }

            if (shouldResume) {
                mPlayer.resume();
            } else {
                mPreviousSongTime = 0;
                mPlayer.play(mTracks.get(mSongIndex).first.getUri());
            }

            mPreviousTime = SystemClock.elapsedRealtime();
            scheduleSeekBarUpdate();

            mEventManager.postEvent(new SongChangeEvent(mTracks.get(mSongIndex).first, mIsPlaying));
        }
    }

    private void pauseMusic() {
        Logger.d(TAG, "Pausing music");

        mPlayer.pause();
        stopSeekBarUpdate();

        mIsPlaying = false;

        updateNotification();
        unregisterReceiver(mAudioBroadcastReceiver);

        if (mIsForeground) {
            stopForeground(false);
            mIsForeground = false;
        }

        mEventManager.postEvent(new SongChangeEvent(mTracks.get(mSongIndex).first, mIsPlaying));
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

        playMusic(false);

        if (!shouldPlaySong) {
            pauseMusic();
        }
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

        playMusic(false);

        if (!shouldPlaySong) {
            pauseMusic();
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
                    PROGRESS_UPDATE_INTERVAL, TimeUnit.MILLISECONDS
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
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!mIsPlaying) {
                    playMusic(true);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                abandonFocus();
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mIsPlaying) {
                    pauseMusic();
                }
                break;
        }
    }

    private void setAsForegroundService() {
        mIsForeground = true;

        Logger.d(TAG, "Setting as foreground service");

        Intent intent = new Intent(getApplicationContext(), NowPlayingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.default_playlist);
        Bitmap defaultPlaylist = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(defaultPlaylist);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        Track currentTrack = mTracks.get(mSongIndex).first;

        String artistNames = "";

        for (Artist artist : currentTrack.getArtists()) {
            artistNames += artist.getName() + ", ";
        }

        artistNames = artistNames.substring(0, artistNames.length() - 2);

        mNotificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_headphones)
                .setContentTitle(currentTrack.getName())
                .setContentText(artistNames)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.teal_gray))
                .setLargeIcon(defaultPlaylist)
                .setAutoCancel(true)
                .setWhen(0)
                .setShowWhen(false)
                .addAction(R.drawable.ic_skip_previous, "Skip Previous", mSkipBackwardIntent);

        if (mIsPlaying) {
            mNotificationBuilder.addAction(R.drawable.ic_pause_small, "Pause", mPauseIntent);
        } else {
            mNotificationBuilder.addAction(R.drawable.ic_play_small, "Play", mPlayIntent);
        }

        mNotificationBuilder.addAction(R.drawable.ic_skip_next, "Skip Next", mSkipForwardIntent);

        android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle();

        style.setShowActionsInCompactView(0, 1, 2);

        Intent cancelIntent = new Intent(getApplicationContext(), MusicPlayer.class);
        cancelIntent.setAction(CLOSE_PLAYER);

        style.setCancelButtonIntent(PendingIntent.getService(getApplicationContext(), REQUEST_CODE, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        style.setShowCancelButton(true);

        mNotificationBuilder.setStyle(style);

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());

        Picasso.with(getApplicationContext())
                .load(currentTrack.getAlbum().getImages().get(0).getUrl())
                .into(mTarget);
    }

    private void updateNotification() {
        Logger.d(TAG, "Updating notification");

        mNotificationBuilder.mActions.remove(1);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_play_small, "Play", mPlayIntent).build();

        mNotificationBuilder.mActions.add(1, action);

        Intent delIntent = new Intent(getApplicationContext(), MusicPlayer.class);
        delIntent.setAction(CLOSE_PLAYER);

        PendingIntent deleteIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE, delIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotificationBuilder.setDeleteIntent(deleteIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updateIcon(Bitmap bitmap) {
        mNotificationBuilder.setLargeIcon(bitmap);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.notify(NOTIFICATION_ID, mNotificationBuilder.build());
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
