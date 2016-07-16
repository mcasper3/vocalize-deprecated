package me.mikecasper.musicvoice.track;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;
import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.responses.TrackResponse;
import me.mikecasper.musicvoice.api.responses.TrackResponseItem;
import me.mikecasper.musicvoice.models.Playlist;
import me.mikecasper.musicvoice.models.SpotifyUser;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.playlist.PlaylistFragment;
import me.mikecasper.musicvoice.playlist.events.GetPlaylistTracksEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.SetPlaylistEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.track.events.TracksObtainedEvent;
import me.mikecasper.musicvoice.util.Logger;
import me.mikecasper.musicvoice.util.RecyclerViewItemClickedEvent;
import me.mikecasper.musicvoice.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class TrackFragment extends Fragment {

    private static final String TAG = "TrackFragment";
    private static final String TRACKS = "tracks";

    private List<TrackResponseItem> mTracks;
    private IEventManager mEventManager;
    private Playlist mPlaylist;

    public TrackFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventManager = EventManagerProvider.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);

        if (savedInstanceState == null) {
            mTracks = new ArrayList<>();
            getTracks();
        } else {
            mTracks = savedInstanceState.getParcelableArrayList(TRACKS);
        }

        // Set the adapter
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.track_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TrackAdapter(mTracks));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        RecyclerFastScroller scrollbar = (RecyclerFastScroller) view.findViewById(R.id.tracks_scrollbar);
        scrollbar.attachRecyclerView(recyclerView);

        return view;
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

        Bundle args = getArguments();

        if (args != null && args.containsKey(PlaylistFragment.SELECTED_PLAYLIST)) {

            ActionBar actionBar = ((MusicVoiceActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(mPlaylist.getName());
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mPlaylist = savedInstanceState.getParcelable(PlaylistFragment.SELECTED_PLAYLIST);

            View view = getView();
            if (view != null) {
                View progressBar = view.findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PlaylistFragment.SELECTED_PLAYLIST, mPlaylist);
        outState.putParcelableArrayList(TRACKS, (ArrayList<TrackResponseItem>) mTracks);
    }

    private void getTracks() {
        Bundle args = getArguments();

        if (args != null && args.containsKey(PlaylistFragment.SELECTED_PLAYLIST)) {
            mPlaylist = args.getParcelable(PlaylistFragment.SELECTED_PLAYLIST);

            if (mPlaylist != null) {
                SpotifyUser owner = mPlaylist.getOwner();
                mEventManager.postEvent(new GetPlaylistTracksEvent(owner.getId(), mPlaylist.getId(), 0));
                mTracks.clear();
            }
        }
    }

    @Subscribe
    public void onTracksObtained(TracksObtainedEvent event) {
        if (event.getPlaylistId().equals(mPlaylist.getId())) {
            TrackResponse response = event.getTrackResponse();

            mTracks.addAll(response.getItems());

            if (response.getNext() == null) {
                View view = getView();
                if (view != null) {
                    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.track_list);
                    TrackAdapter adapter = (TrackAdapter) recyclerView.getAdapter();
                    adapter.updateTracks(mTracks);

                    View progressBar = view.findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.INVISIBLE);

                    View shufflePlay = view.findViewById(R.id.shuffle_play_button);
                    shufflePlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shufflePlay();
                        }
                    });

                    Logger.d(TAG, "Tracks obtained");
                }
            } else {
                mEventManager.postEvent(new GetPlaylistTracksEvent(event.getUserId(), event.getPlaylistId(), response.getOffset() + response.getItems().size()));
            }
        }
    }

    private void shufflePlay() {
        if (mTracks != null) {
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit()
                    .putBoolean(NowPlayingActivity.SHUFFLE_ENABLED, true)
                    .apply();

            int position = (int) (Math.random() * mTracks.size());
            Track track = mTracks.get(position).getTrack();

            mEventManager.postEvent(new ToggleShuffleEvent(true));

            moveToNowPlayingIfNeeded(position, track);
        }
    }

    @Subscribe
    public void onItemClick(RecyclerViewItemClickedEvent event) {
        RecyclerView.ViewHolder viewHolder = event.getViewHolder();

        if (viewHolder instanceof TrackAdapter.ViewHolder) {
            TrackAdapter.ViewHolder selectedTrack = (TrackAdapter.ViewHolder) viewHolder;
            Track track = selectedTrack.mTrack;

            int position = viewHolder.getAdapterPosition();

            moveToNowPlayingIfNeeded(position, track);
        }
    }

    private void moveToNowPlayingIfNeeded(int position, Track track) {
        List<TrackResponseItem> copy = new ArrayList<>(mTracks.size());

        for (TrackResponseItem item : mTracks) {
            copy.add(new TrackResponseItem(item));
        }

        mEventManager.postEvent(new SetPlaylistEvent(copy, position));

        Activity activity = getActivity();

        if (activity != null) {
            View miniNowPlaying = activity.findViewById(R.id.main_music_controls);
            if (miniNowPlaying == null || miniNowPlaying.getVisibility() != View.VISIBLE) {
                Intent intent = new Intent(getContext(), NowPlayingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(NowPlayingActivity.TRACK, track);
                intent.putExtra(NowPlayingActivity.SHOULD_PLAY_TRACK, true);
                intent.putExtra(NowPlayingActivity.IS_PLAYING_MUSIC, false);
                intent.putExtra(NowPlayingActivity.CURRENT_TIME, 0);
                intent.putExtra(NowPlayingActivity.PLAYLIST_NAME, mPlaylist.getName());
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        mEventManager = null;
        mTracks = null;
        mPlaylist = null;

        ((MusicVoiceApplication) getActivity().getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }
}
