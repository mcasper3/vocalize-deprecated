package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.events.AddTracksToPriorityQueueEvent;
import me.mikecasper.musicvoice.nowplaying.events.CheckBoxSelectedEvent;
import me.mikecasper.musicvoice.nowplaying.events.RemoveTracksFromQueueEvent;
import me.mikecasper.musicvoice.nowplaying.models.QueueItemInformation;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.util.RecyclerViewItemClickedEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.GetQueuesEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.PlaySongFromQueueEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.QueuesObtainedEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.util.Logger;
import me.mikecasper.musicvoice.views.DividerItemDecoration;

public class QueueFragment extends Fragment {

    private static final String TAG = "QueueFragment";

    private IEventManager mEventManager;
    private List<Track> mQueue;
    private List<Track> mPriorityQueue;
    private List<QueueItemInformation> mTracksSelected;
    private Track mNowPlaying;
    private ProgressBar mProgressBar;
    private boolean mIsPlaying;

    public QueueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventManager = EventManagerProvider.getInstance(getContext());
        mTracksSelected = new ArrayList<>();
    }

    @Override
    public void onPause() {
        mEventManager.unregister(this);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mEventManager.register(this);
        mEventManager.postEvent(new GetQueuesEvent());
        mEventManager.postEvent(new GetPlayerStatusEvent());
    }

    @Override
    public void onDestroy() {
        mEventManager = null;
        mQueue = null;
        mPriorityQueue = null;
        mProgressBar = null;
        mNowPlaying = null;

        ((MusicVoiceApplication) getActivity().getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);

        mQueue = new ArrayList<>();
        mPriorityQueue = new ArrayList<>();
        mNowPlaying = null;

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String playlistName = args.getString(NowPlayingActivity.PLAYLIST_NAME, null);
        Track track = args.getParcelable(NowPlayingActivity.TRACK);
        mIsPlaying = args.getBoolean(NowPlayingActivity.IS_PLAYING_MUSIC, false);

        if (mIsPlaying) {
            ImageView pausePlayImage = (ImageView) view.findViewById(R.id.queue_play_pause_image);
            pausePlayImage.setImageResource(R.drawable.ic_pause);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (playlistName == null) {
            playlistName = sharedPreferences.getString(NowPlayingActivity.PLAYLIST_NAME, null);
        } else {
            sharedPreferences.edit()
                    .putString(NowPlayingActivity.PLAYLIST_NAME, playlistName)
                    .apply();
        }

        TextView playlistNameTextView = (TextView) view.findViewById(R.id.playlist_name);
        playlistNameTextView.setSelected(true);
        playlistNameTextView.setSingleLine(true);
        playlistNameTextView.setText(getString(R.string.playing_from, playlistName));

        ItemTouchHelper songDragHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        });

        // Set the adapter
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.queue_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new QueueAdapter(mNowPlaying, mQueue, mPriorityQueue, playlistName));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        View nowPlayingButton = view.findViewById(R.id.now_playing_button);
        nowPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mProgressBar = (ProgressBar) view.findViewById(R.id.queue_song_time);

        if (track != null) {
            mProgressBar.setMax(track.getDuration());
        }

        setUpButtons(view);
    }

    private void setUpButtons(View view) {
        View previousButton = view.findViewById(R.id.queue_skip_previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipBackwardEvent());
            }
        });

        View nextButton = view.findViewById(R.id.queue_skip_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new SkipForwardEvent());
            }
        });

        View playPauseButton = view.findViewById(R.id.queue_play_pause_button);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPlaying = !mIsPlaying;
                mEventManager.postEvent(new TogglePlaybackEvent());

                updatePlayButton();
            }
        });

        View removeButton = view.findViewById(R.id.queue_remove_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new RemoveTracksFromQueueEvent(mTracksSelected));
                mTracksSelected.clear();
                updateBottomSection(true);
            }
        });

        View addToQueueButton = view.findViewById(R.id.queue_add_button);
        addToQueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new AddTracksToPriorityQueueEvent(mTracksSelected));
                mTracksSelected.clear();
                updateBottomSection(true);
            }
        });
    }

    @Subscribe
    public void onSongTimeUpdated(UpdateSongTimeEvent event) {
        mProgressBar.setProgress(event.getSongTime());
    }

    @Subscribe
    public void onPlayerStatusUpdated(UpdatePlayerStatusEvent event) {
        Track track = event.getTrack();

        if (track == null) {
            getActivity().finish();
        } else {
            mProgressBar.setMax(track.getDuration());

            boolean wasPlaying = mIsPlaying;
            mIsPlaying = event.isPlaying();

            if (wasPlaying != mIsPlaying) {
                updatePlayButton();
            }
        }
    }

    @Subscribe
    public void onQueuesObtained(QueuesObtainedEvent event) {
        mQueue = event.getQueue();
        mPriorityQueue = event.getPriorityQueue();
        mNowPlaying = event.getNowPlaying();

        View view = getView();
        if (view != null) {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.queue_list);
            QueueAdapter adapter = (QueueAdapter) recyclerView.getAdapter();
            adapter.updateQueues(mQueue, mPriorityQueue, mNowPlaying);

            Logger.d(TAG, "Queues obtained");
        }
    }

    @Subscribe
    public void onItemClick(RecyclerViewItemClickedEvent event) {
        RecyclerView.ViewHolder viewHolder = event.getViewHolder();

        if (viewHolder instanceof QueueAdapter.NowPlayingViewHolder) {
            getFragmentManager().popBackStack();
        } else if (viewHolder instanceof QueueAdapter.QueueItemViewHolder) {
            QueueAdapter.QueueItemViewHolder queueItemViewHolder = (QueueAdapter.QueueItemViewHolder) viewHolder;
            mEventManager.postEvent(new PlaySongFromQueueEvent(queueItemViewHolder.mTrackIndex, queueItemViewHolder.mIsPriorityQueue));
        }
    }

    @Subscribe
    public void onCheckBoxSelected(CheckBoxSelectedEvent event) {
        boolean wasEmpty = mTracksSelected.isEmpty();

        if (event.isSelected()) {
            mTracksSelected.add(event.getItemInformation());
        } else {
            mTracksSelected.remove(event.getItemInformation());
        }

        int size = mTracksSelected.size();
        if (size == 1 && wasEmpty) {
            updateBottomSection(false);
        } else if (size == 0) {
            updateBottomSection(true);
        }
    }

    // View methods
    private void updateBottomSection(boolean controlsVisible) {
        View view = getView();

        if (view != null) {
            View musicControls = view.findViewById(R.id.queue_music_controls);
            View alternateSection = view.findViewById(R.id.queue_alternate_controls);

            if (musicControls != null && alternateSection != null) {
                int controlsVisibility = controlsVisible ? View.VISIBLE : View.GONE;
                int alternateSectionVisibility = controlsVisible ? View.GONE : View.VISIBLE;

                musicControls.setVisibility(controlsVisibility);
                alternateSection.setVisibility(alternateSectionVisibility);
            }
        }
    }

    @Subscribe
    public void onSongChange(SongChangeEvent event) {
        boolean wasPlaying = mIsPlaying;
        mIsPlaying = event.isPlayingSong();

        if (wasPlaying != mIsPlaying) {
            updatePlayButton();
        }

        Track track = event.getTrack();
        if (event.getTrack() != null) {
            mProgressBar.setMax(track.getDuration());
        }
    }

    @Subscribe
    public void onLostPermission(LostPermissionEvent event) {
        if (mIsPlaying) {
            mIsPlaying = false;
            updatePlayButton();
        }
    }

    private void updatePlayButton() {
        View view = getView();

        if (view != null) {
            ImageView imageView = (ImageView) view.findViewById(R.id.queue_play_pause_image);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animatePlayButton(imageView);
            } else {
                int id = mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play;

                imageView.setImageResource(id);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton(ImageView imageView) {
        AnimatedVectorDrawable drawable;
        if (mIsPlaying) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_play_to_pause);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_pause_to_play);
        }

        imageView.setImageDrawable(drawable);
        drawable.start();
    }

    public interface ItemTouchAdapterListener {
        boolean onItemMove(int fromPosition, int toPosition);
    }
}
