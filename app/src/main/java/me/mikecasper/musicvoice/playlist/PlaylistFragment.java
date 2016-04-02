package me.mikecasper.musicvoice.playlist;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Playlist;
import me.mikecasper.musicvoice.playlist.events.PlaylistsObtainedEvent;
import me.mikecasper.musicvoice.services.EventManager;
import me.mikecasper.musicvoice.services.EventManagerProvider;
import me.mikecasper.musicvoice.util.Utility;

public class PlaylistFragment extends Fragment {

    private static final String TAG = "PlaylistFragment";

    private List<Playlist> mPlaylists;
    private EventManager mEventManager;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlaylists = new ArrayList<>();
        mEventManager = EventManagerProvider.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView playlistRecyclerView = (RecyclerView) view.findViewById(R.id.playlistGrid);
        playlistRecyclerView.setHasFixedSize(true);
        playlistRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        playlistRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(Utility.convertDpToPixel(10, getContext())));
        playlistRecyclerView.setAdapter(new PlaylistAdapter(getContext(), mPlaylists));
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

    @Subscribe
    public void onPlaylistsObtained(PlaylistsObtainedEvent event) {
        Log.i(TAG, "Playlists obtained");

        mPlaylists = event.getPlaylists();

        View view = getView();

        if (view != null) {
            RecyclerView playlistRecyclerView = (RecyclerView) view.findViewById(R.id.playlistGrid);
            PlaylistAdapter playlistAdapter = (PlaylistAdapter) playlistRecyclerView.getAdapter();
            playlistAdapter.setPlaylists(mPlaylists);
            playlistAdapter.notifyDataSetChanged();
        }
    }
}
