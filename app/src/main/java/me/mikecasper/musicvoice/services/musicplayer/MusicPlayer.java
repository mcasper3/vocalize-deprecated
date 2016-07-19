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
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

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
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
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
import me.mikecasper.musicvoice.nowplaying.events.AddTracksToPriorityQueueEvent;
import me.mikecasper.musicvoice.nowplaying.events.RemoveTracksFromQueueEvent;
import me.mikecasper.musicvoice.nowplaying.models.QueueItemInformation;
import me.mikecasper.musicvoice.services.AudioBroadcastReceiver;
import me.mikecasper.musicvoice.services.HeadphoneBroadcastReceiver;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.*;
import me.mikecasper.musicvoice.services.voicerecognition.IVoiceRecognizer;
import me.mikecasper.musicvoice.services.voicerecognition.PocketSphinxVoiceRecognizer;
import me.mikecasper.musicvoice.util.Logger;

public class MusicPlayer extends Service implements ConnectionStateCallback, PlayerNotificationCallback, AudioManager.OnAudioFocusChangeListener {

    private static boolean sIsAlive;

    public static boolean isAlive() {
        return sIsAlive;
    }

    private static final String TAG = "MusicPlayer";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 37;
    private static final int QUEUE_SIZE = 50;

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
    private boolean mWasPlaying;
    private boolean mPlayingFromPriorityQueue;
    private boolean mRecentlyPlayedMusic;
    private int mRepeatMode;
    private int mSongIndex;
    private int mPlaylistSize;
    private int mPreviousSongIndex;
    private int mNextSongIndex;
    private List<Integer> mNewTrackOrder;
    private Stack<Integer> mTrackHistory;
    private Deque<Integer> mQueue;
    private Deque<Integer> mPriorityQueue;
    private List<Track> mOriginalTracks;
    private IEventManager mEventManager;
    private AudioManager mAudioManager;
    private BroadcastReceiver mAudioBroadcastReceiver;
    private BroadcastReceiver mHeadphonesBroadcastReceiver;
    private final IntentFilter mIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final IntentFilter mHeadphoneFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    private NotificationCompat.Builder mNotificationBuilder;

    // Voice recognition
    private IVoiceRecognizer mVoiceRecognizer;

    // Intents for notification
    private PendingIntent mPlayIntent;
    private PendingIntent mPauseIntent;
    private PendingIntent mSkipForwardIntent;
    private PendingIntent mSkipBackwardIntent;

    // Target for notification icons
    private Target mTarget;

    // Song time stuff
    private static final long PROGRESS_UPDATE_INTERVAL = 100;
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

        mVoiceRecognizer = new PocketSphinxVoiceRecognizer(this);

        mPreviousSongIndex = 0;
        mNextSongIndex = QUEUE_SIZE;

        mNewTrackOrder = new ArrayList<>();
        mTrackHistory = new Stack<>();
        mQueue = new LinkedList<>();
        mPriorityQueue = new LinkedList<>();
        mOriginalTracks = new ArrayList<>();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
        mHeadphonesBroadcastReceiver = new HeadphoneBroadcastReceiver();

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

        mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, null, mPreviousSongTime));
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
                initPlayer();
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
        playerConfig.useCache(false);

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
    }

    @Subscribe
    public void onDestroyPlayer(DestroyPlayerEvent event) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Logger.d(TAG, "In onDestroy");

        sIsAlive = false;

        mEventManager.unregister(this);

        if (mIsPlaying) {
            int position;
            if (mPlayingFromPriorityQueue) {
                position = mPriorityQueue.peekFirst();
            } else {
                position = mQueue.peekFirst();
            }
            Track track = mOriginalTracks.get(position);
            mEventManager.postEvent(new UpdatePlayerStatusEvent(false, track, mPreviousSongTime));
            mPlayer.pause();
            mIsPlaying = false;
        }

        mVoiceRecognizer.stopListening();
        abandonFocus();
        mHasFocus = false;
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
    public void onPlayMusic(PlayMusicEvent event) {
        Logger.d(TAG, "On play music");

        playMusic(true);
    }

    @Subscribe
    public void onPauseMusic(PauseMusicEvent event) {
        Logger.d(TAG, "On pause music");

        pauseMusic();
    }

    @Subscribe
    public void onToggleShuffle(ToggleShuffleEvent event) {
        mShuffleWasEnabled = mShuffleEnabled;

        mShuffleEnabled = event.shouldOverrideShuffle() || !mShuffleEnabled;

        if (mOriginalTracks != null && mOriginalTracks.size() > 0) {
            organizeTracks(false, -1);
        }
    }

    @Subscribe
    public void onToggleRepeat(ToggleRepeatEvent event) {
        mRepeatMode = ++mRepeatMode % 3;

        createQueue(false);
    }

    @Subscribe
    public void onBeginListening(BeginListeningEvent event) {
        mVoiceRecognizer.startListening();
    }

    @Subscribe
    public void onPauseListening(PauseListeningEvent event) {
        mVoiceRecognizer.stopListening();
    }

    @Subscribe
    public void onSetPlaylist(SetPlaylistEvent event) {
        List<TrackResponseItem> items = event.getTracks();

        mPlaylistSize = items.size();
        mOriginalTracks.clear();

        for (TrackResponseItem item : items) {
            mOriginalTracks.add(item.getTrack());
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            mVoiceRecognizer.startListening();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mShuffleEnabled = sharedPreferences.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED, false);

        int position = event.getPosition();
        organizeTracks(true, position);
        mSongIndex = 0;
        mPreviousSongIndex = mPlaylistSize - 1;
        mPlayingFromPriorityQueue = false;

        if (mPlayer == null) {
            initPlayer();
        }

        playMusic(false);
    }

    private void createQueue(boolean recreateCompletely) {
        int size;
        boolean repeatEnabled = mRepeatMode == NowPlayingActivity.MODE_ENABLED;

        if (!repeatEnabled) {
            int position = mNewTrackOrder.get(mSongIndex);

            size = Math.min(QUEUE_SIZE, mPlaylistSize - position);
        } else {
            size = QUEUE_SIZE;
        }

        if (!recreateCompletely) {
            size -= mQueue.size();
        } else {
            mNextSongIndex = 0;

            if (mQueue.size() > 0) {
                mQueue.clear();
            }
        }

        for (int i = 0; i < size; i++) {
            mQueue.add(mNewTrackOrder.get(mNextSongIndex++ % mPlaylistSize));
        }
    }

    private void addToQueue(boolean addToFront) {
        boolean repeatEnabled = mRepeatMode == NowPlayingActivity.MODE_ENABLED;

        if (addToFront) {
            if (!mTrackHistory.isEmpty()) {
                int previousSong = mTrackHistory.pop();
                mQueue.addFirst(previousSong);
            } else {
                mQueue.addFirst(mNewTrackOrder.get(mPreviousSongIndex));
                mPreviousSongIndex--;

                if (mPreviousSongIndex == -1) {
                    mPreviousSongIndex = mPlaylistSize - 1;
                }
            }
        } else {
            int lastPlayed = mQueue.removeFirst();
            mTrackHistory.push(lastPlayed);

            if (mQueue.size() < QUEUE_SIZE) {
                int position;

                if (mShuffleEnabled) {
                    position = mSongIndex;
                } else {
                    position = mNewTrackOrder.get(mSongIndex);
                }

                int nextPosition = position + QUEUE_SIZE - 1;
                if (repeatEnabled && nextPosition > mPlaylistSize - 1) {
                    if (mShuffleEnabled) {
                        mQueue.addLast(mNewTrackOrder.get(nextPosition % mPlaylistSize));
                    } else {
                        mQueue.addLast(nextPosition % mPlaylistSize);
                    }
                } else if (nextPosition <= mPlaylistSize - 1) {
                    if (mShuffleEnabled) {
                        mQueue.addLast(mNewTrackOrder.get(nextPosition));
                    } else {
                        mQueue.addLast(nextPosition);
                    }
                }
            }
        }
    }

    @Subscribe
    public void onGetQueues(GetQueuesEvent event) {
        int queueSize = mQueue.size();

        int size = Math.min(QUEUE_SIZE, queueSize - 1);

        List<Track> queue = new ArrayList<>(size);
        List<Track> priorityQueue = new ArrayList<>(mPriorityQueue.size());

        boolean foundLastSong = false;

        Iterator<Integer> iterator = mQueue.iterator();
        Iterator<Integer> priorityIterator = mPriorityQueue.iterator();
        Track firstTrack;

        int prioritySize = mPriorityQueue.size();

        if (!mPlayingFromPriorityQueue) {
            firstTrack = mOriginalTracks.get(iterator.next());
        } else {
            firstTrack = mOriginalTracks.get(priorityIterator.next());
            prioritySize--;
        }

        for (int i = 0; i < size; i++) {
            int current = iterator.next();
            Track track = mOriginalTracks.get(current);
            queue.add(track);

            if ((mShuffleEnabled && current == mNewTrackOrder.get(mPlaylistSize - 1)) || current == mPlaylistSize - 1) {
                if (foundLastSong) {
                    break;
                } else {
                    foundLastSong = true;
                }
            }
        }

        for (int i = 0; i < prioritySize; i++) {
            Track track = mOriginalTracks.get(priorityIterator.next());
            priorityQueue.add(track);
        }

        mEventManager.postEvent(new QueuesObtainedEvent(firstTrack, queue, priorityQueue));
    }

    private void organizeTracks(boolean refreshTracks, int position) {
        int firstTrack;

        if ((mShuffleWasEnabled && !mShuffleEnabled) || refreshTracks) {
            int index = position;

            if (!refreshTracks) {
                index = mNewTrackOrder.get(mSongIndex);
            }

            mNewTrackOrder.clear();

            for (int i = index; i < mOriginalTracks.size(); i++) {
                mNewTrackOrder.add(i);
            }

            for (int i = 0; i < index; i++) {
                mNewTrackOrder.add(i);
            }

            firstTrack = mNewTrackOrder.remove(0);
        } else {
            firstTrack = mNewTrackOrder.remove(mSongIndex);
        }

        if (mShuffleEnabled) {
            Collections.shuffle(mNewTrackOrder);
        }

        mNewTrackOrder.add(0, firstTrack);
        mSongIndex = 0;

        createQueue(true);
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
        mIsForeground = false;

        if (!mNewTrackOrder.isEmpty()) {
            int position = mQueue.peekFirst();
            track = mOriginalTracks.get(position);
        }

        mEventManager.postEvent(new UpdatePlayerStatusEvent(mIsPlaying, track, mPreviousSongTime));
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

            registerReceiver(mHeadphonesBroadcastReceiver, mHeadphoneFilter);

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.isWiredHeadsetOn()) {
                mVoiceRecognizer.startListening();
            }

            if (mIsForeground) {
                setAsForegroundService();
            }

            int position;

            if (mPlayingFromPriorityQueue) {
                position = mPriorityQueue.peekFirst();
            } else {
                position = mQueue.peekFirst();
            }

            Track track = mOriginalTracks.get(position);

            if (shouldResume) {
                if (!mIsPlaying) {
                    mPlayer.resume();
                }
            } else {
                mRecentlyPlayedMusic = true;
                mPreviousSongTime = 0;
                mPlayer.play(track.getUri());
            }

            mPreviousTime = SystemClock.elapsedRealtime();
            scheduleSeekBarUpdate();

            mEventManager.postEvent(new SongChangeEvent(track, mIsPlaying));
        }
    }

    private void pauseMusic() {
        Logger.d(TAG, "Pausing music");

        mPlayer.pause();
        stopSeekBarUpdate();

        mIsPlaying = false;

        unregisterReceiver(mAudioBroadcastReceiver);
        unregisterReceiver(mHeadphonesBroadcastReceiver);

        if (mIsForeground) {
            updateNotification();
            stopForeground(false);
        }

        int position = mQueue.peekFirst();
        Track track = mOriginalTracks.get(position);
        mEventManager.postEvent(new SongChangeEvent(track, mIsPlaying));
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
            if (mPlayingFromPriorityQueue) {
                mPriorityQueue.removeFirst();
            }

            if (mPriorityQueue.isEmpty()) {
                mPlayingFromPriorityQueue = false;
                mSongIndex = ++mSongIndex % mPlaylistSize;

                if ((!mShuffleEnabled && mNewTrackOrder.get(mSongIndex) == 0) || (mShuffleEnabled && mSongIndex == 0)) {
                    if (mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                        shouldPlaySong = false;
                        createQueue(true);
                    } else {
                        addToQueue(false);
                    }
                } else {
                    addToQueue(false);
                }
            } else {
                if (!mPlayingFromPriorityQueue) {
                    mTrackHistory.push(mQueue.removeFirst());
                }

                mPlayingFromPriorityQueue = true;
            }
        }

        playMusic(false);

        if (!shouldPlaySong) {
            pauseMusic();
        }

        onGetQueues(null);
    }

    private void playPreviousSong() {
        boolean shouldPlaySong = true;
        mPlayingFromPriorityQueue = false;

        --mSongIndex;

        if (mSongIndex == -1) {
            mSongIndex = mPlaylistSize - 1;

            if (mShuffleEnabled && mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
                mSongIndex = 0;
                shouldPlaySong = false;
            } else {
                addToQueue(true);
            }
        } else {
            addToQueue(true);
        }

        if (!mShuffleEnabled && mNewTrackOrder.get(mSongIndex) == mPlaylistSize - 1 && mRepeatMode != NowPlayingActivity.MODE_ENABLED) {
            shouldPlaySong = false;
        }

        playMusic(false);

        if (!shouldPlaySong) {
            pauseMusic();
        }

        onGetQueues(null);
    }

    @Subscribe
    public void onAddSongsToPriorityQueue(AddTracksToPriorityQueueEvent event) {
        List<QueueItemInformation> trackList = event.getTracksToAdd();

        Collections.sort(trackList, new Comparator<QueueItemInformation>() {
            @Override
            public int compare(QueueItemInformation lhs, QueueItemInformation rhs) {
                if (lhs.isFromPriorityQueue() && !rhs.isFromPriorityQueue()) {
                    return -1;
                } else if (rhs.isFromPriorityQueue() && !lhs.isFromPriorityQueue()) {
                    return 1;
                } else {
                    return lhs.getTrackIndex() - rhs.getTrackIndex();
                }
            }
        });

        Iterator<Integer> priorityIterator = mPriorityQueue.iterator();
        Iterator<Integer> queueIterator = mQueue.iterator();

        if (mPlayingFromPriorityQueue) {
            priorityIterator.next();
        } else {
            queueIterator.next();
        }

        List<Integer> songsToAdd = new ArrayList<>();

        int priorityIndex = 0;
        int queueIndex = 0;
        for (QueueItemInformation info : trackList) {
            if (info.isFromPriorityQueue()) {
                for (; priorityIndex < info.getTrackIndex(); priorityIndex++) {
                    priorityIterator.next();
                }

                songsToAdd.add(priorityIterator.next());
                priorityIndex++;
            } else {
                for (; queueIndex < info.getTrackIndex(); queueIndex++) {
                    queueIterator.next();
                }

                songsToAdd.add(queueIterator.next());
                queueIndex++;
            }
        }

        for (int song : songsToAdd) {
            mPriorityQueue.add(song);
        }

        onGetQueues(null);
    }

    @Subscribe
    public void onRemoveSongsFromQueue(RemoveTracksFromQueueEvent event) {
        List<QueueItemInformation> trackList = event.getTracksToRemove();

        Collections.sort(trackList, new Comparator<QueueItemInformation>() {
            @Override
            public int compare(QueueItemInformation lhs, QueueItemInformation rhs) {
                if (lhs.isFromPriorityQueue() && !rhs.isFromPriorityQueue()) {
                    return 1;
                } else if (rhs.isFromPriorityQueue() && !lhs.isFromPriorityQueue()) {
                    return -1;
                } else {
                    return lhs.getTrackIndex() - rhs.getTrackIndex();
                }
            }
        });

        Iterator<Integer> priorityIterator = mPriorityQueue.iterator();
        Iterator<Integer> queueIterator = mQueue.iterator();

        int priorityIndex = 0;
        int queueIndex = 0;
        for (QueueItemInformation info : trackList) {
            if (info.isFromPriorityQueue()) {
                for (; priorityIndex < info.getTrackIndex() + 1; priorityIndex++) {
                    priorityIterator.next();
                }

                priorityIterator.remove();
            } else {
                for (; queueIndex < info.getTrackIndex() + 1; queueIndex++) {
                    queueIterator.next();
                }

                queueIterator.remove();
            }
        }

        createQueue(false);
        onGetQueues(null);
    }

    @Subscribe
    public void playSongFromQueue(PlaySongFromQueueEvent event) {
        int queueDifference = event.getQueueIndex();

        if (event.isPriorityQueue()) {
            if (mPlayingFromPriorityQueue) {
                queueDifference++;
            } else {
                mTrackHistory.push(mQueue.removeFirst());
            }

            mPlayingFromPriorityQueue = true;

            for (int i = 0; i < queueDifference; i++) {
                mPriorityQueue.removeFirst();
            }

            playMusic(false);
            onGetQueues(null);
        } else {
            if (!mPlayingFromPriorityQueue) {
                queueDifference++;
            }

            mSongIndex = (mSongIndex + queueDifference) % mPlaylistSize;

            int current;
            for (int i = 0; i < queueDifference; i++) {
                current = mQueue.removeFirst();
                mTrackHistory.push(current);
            }

            mPlayingFromPriorityQueue = false;
            playMusic(false);
            createQueue(false);
            onGetQueues(null);
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

        if (mIsPlaying) {
            pauseMusic();
        }

        stopSelf();
    }

    @Override
    public void onTemporaryError() {
        Logger.d(TAG, "Temporary error");

        if (mIsPlaying) {
            pauseMusic();
            Toast.makeText(MusicPlayer.this, R.string.temporary_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionMessage(String s) {
        Logger.d(TAG, s);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Logger.i(TAG, eventType.name());

        switch (eventType) {
            case TRACK_CHANGED:
                if (mRecentlyPlayedMusic) {
                    mRecentlyPlayedMusic = false;
                } else {
                    playNextSong();
                }
                break;
            case LOST_PERMISSION:
                if (mIsPlaying) {
                    pauseMusic();
                }
                mEventManager.postEvent(new LostPermissionEvent());
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        Logger.e(TAG, "Playback error: " + errorType.name() + "; s");
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
                if (!mIsPlaying && mWasPlaying) {
                    playMusic(true);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                abandonFocus();
                mHasFocus = false;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mIsPlaying) {
                    pauseMusic();
                    mWasPlaying = true;
                } else {
                    mWasPlaying = false;
                    mVoiceRecognizer.stopListening();
                }
                break;
        }
    }

    private void setAsForegroundService() {
        if (!mQueue.isEmpty()) {
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

            int position = mQueue.peekFirst();
            Track currentTrack = mOriginalTracks.get(position);

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
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGray))
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

            if (!mIsPlaying) {
                stopForeground(false);
            }
        }
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
