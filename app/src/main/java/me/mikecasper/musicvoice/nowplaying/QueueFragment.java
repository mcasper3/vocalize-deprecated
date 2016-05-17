package me.mikecasper.musicvoice.nowplaying;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.GetQueuesEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.OnQueuesObtainedEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.util.Logger;
import me.mikecasper.musicvoice.util.RecyclerViewItemClickListener;
import me.mikecasper.musicvoice.views.DividerItemDecoration;

public class QueueFragment extends Fragment implements RecyclerViewItemClickListener {

    private static final String TAG = "QueueFragment";

    private IEventManager mEventManager;
    private List<Track> mQueue;
    private List<Track> mPriorityQueue;

    public QueueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventManager = EventManagerProvider.getInstance(getContext());
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
    }

    @Override
    public void onDestroy() {
        ((MusicVoiceApplication) getActivity().getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);

        getQueues();
        mQueue = new ArrayList<>();
        mPriorityQueue = new ArrayList<>();

        Bundle args = getArguments();
        String playlistName = args.getString(NowPlayingActivity.PLAYLIST_NAME, null);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (playlistName == null) {
            playlistName = sharedPreferences.getString(NowPlayingActivity.PLAYLIST_NAME, null);
        } else {
            sharedPreferences.edit()
                    .putString(NowPlayingActivity.PLAYLIST_NAME, playlistName)
                    .apply();
        }

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.track_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new QueueAdapter(mQueue, mPriorityQueue, playlistName, this));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        return view;
    }

    private void getQueues() {
        mEventManager.postEvent(new GetQueuesEvent());
    }

    @Subscribe
    public void onPlayerStatusUpdated(UpdatePlayerStatusEvent event) {
        Track track = event.getTrack();

        if (track == null) {
            getActivity().finish();
        }
    }

    @Subscribe
    public void onQueuesObtained(OnQueuesObtainedEvent event) {
        mQueue = event.getQueue();
        mPriorityQueue = event.getPriorityQueue();

        View view = getView();
        if (view != null) {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.track_list);
            QueueAdapter adapter = (QueueAdapter) recyclerView.getAdapter();
            adapter.updateQueues(mQueue, mPriorityQueue);

            Logger.d(TAG, "Queues obtained");
        }
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof QueueAdapter.NowPlayingViewHolder) {
            // TODO now playing
        } else {
            // TODO queue item
        }
    }
}
