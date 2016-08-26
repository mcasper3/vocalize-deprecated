package me.mikecasper.vocalize.playlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.leakcanary.RefWatcher;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.mikecasper.vocalize.MusicVoiceActivity;
import me.mikecasper.vocalize.MusicVoiceApplication;
import me.mikecasper.vocalize.R;
import me.mikecasper.vocalize.api.responses.PlaylistResponse;
import me.mikecasper.vocalize.models.Playlist;
import me.mikecasper.vocalize.playlist.events.GetPlaylistsEvent;
import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.track.TrackFragment;
import me.mikecasper.vocalize.util.Logger;
import me.mikecasper.vocalize.util.RecyclerViewItemClickedEvent;
import me.mikecasper.vocalize.util.Utility;
import me.mikecasper.vocalize.views.VerticalSpaceItemDecoration;

public class PlaylistFragment extends Fragment {

    public static final String SELECTED_PLAYLIST = "selectedPlaylist";

    private static final String TAG = "PlaylistFragment";

    private List<Playlist> mPlaylists;
    private IEventManager mEventManager;

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

        mEventManager.postEvent(new GetPlaylistsEvent());

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView playlistRecyclerView = (RecyclerView) view.findViewById(R.id.playlist_grid);
        playlistRecyclerView.setHasFixedSize(true);
        playlistRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        playlistRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(Utility.convertDpToPixel(10, getContext())));
        playlistRecyclerView.setAdapter(new PlaylistAdapter(getContext(), mPlaylists));

        if (!mPlaylists.isEmpty()) {
            View progressBar = view.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.INVISIBLE);
        }
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

        ActionBar actionBar = ((MusicVoiceActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_playlists);
        }
    }

    @Subscribe
    public void onPlaylistsObtained(PlaylistResponse response) {
        Logger.i(TAG, "Playlists obtained");

        mPlaylists = response.getPlaylists();

        View view = getView();

        if (view != null) {
            RecyclerView playlistRecyclerView = (RecyclerView) view.findViewById(R.id.playlist_grid);
            PlaylistAdapter playlistAdapter = (PlaylistAdapter) playlistRecyclerView.getAdapter();
            playlistAdapter.setPlaylists(mPlaylists);
            playlistAdapter.notifyDataSetChanged();

            View progressBar = view.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe
    public void onItemClick(RecyclerViewItemClickedEvent event) {
        RecyclerView.ViewHolder viewHolder = event.getViewHolder();

        if (viewHolder instanceof PlaylistAdapter.ViewHolder) {
            PlaylistAdapter.ViewHolder selectedPlaylist = (PlaylistAdapter.ViewHolder) viewHolder;
            Playlist playlist = selectedPlaylist.mPlaylist;

            Fragment fragment = new TrackFragment();
            Bundle args = new Bundle();
            args.putParcelable(SELECTED_PLAYLIST, playlist);
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        mPlaylists = null;
        mEventManager = null;

        RefWatcher watcher = ((MusicVoiceApplication) getActivity().getApplication()).getRefWatcher();
        watcher.watch(this);

        super.onDestroy();
    }
}
