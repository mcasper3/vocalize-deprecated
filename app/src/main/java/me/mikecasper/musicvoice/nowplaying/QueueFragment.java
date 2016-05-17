package me.mikecasper.musicvoice.nowplaying;

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
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.GetQueuesEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.QueuesObtainedEvent;
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
        getQueues();
    }

    @Override
    public void onDestroy() {
        ((MusicVoiceApplication) getActivity().getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);

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

        TextView playlistNameTextView = (TextView) view.findViewById(R.id.playlist_name);
        playlistNameTextView.setSelected(true);
        playlistNameTextView.setSingleLine(true);
        playlistNameTextView.setText(getString(R.string.playing_from, playlistName));

        // Set the adapter
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.queue_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new QueueAdapter(mQueue, mPriorityQueue, playlistName, this));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        View nowPlayingButton = view.findViewById(R.id.now_playing_button);
        nowPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
    public void onQueuesObtained(QueuesObtainedEvent event) {
        mQueue = event.getQueue();
        mPriorityQueue = event.getPriorityQueue();

        View view = getView();
        if (view != null) {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.queue_list);
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
