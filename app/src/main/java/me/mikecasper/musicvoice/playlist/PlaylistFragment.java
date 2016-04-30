package me.mikecasper.musicvoice.playlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.responses.PlaylistResponse;
import me.mikecasper.musicvoice.models.Playlist;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.track.TrackFragment;
import me.mikecasper.musicvoice.util.Logger;
import me.mikecasper.musicvoice.util.RecyclerViewItemClickListener;
import me.mikecasper.musicvoice.util.Utility;
import me.mikecasper.musicvoice.views.VerticalSpaceItemDecoration;

public class PlaylistFragment extends Fragment implements RecyclerViewItemClickListener {

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
        playlistRecyclerView.setAdapter(new PlaylistAdapter(getContext(), mPlaylists, this));
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

        getActivity().setTitle(R.string.title_playlists);
    }

    @Subscribe
    public void onPlaylistsObtained(PlaylistResponse response) {
        Logger.i(TAG, "Playlists obtained");

        mPlaylists = response.getPlaylists();

        View view = getView();

        if (view != null) {
            RecyclerView playlistRecyclerView = (RecyclerView) view.findViewById(R.id.playlistGrid);
            PlaylistAdapter playlistAdapter = (PlaylistAdapter) playlistRecyclerView.getAdapter();
            playlistAdapter.setPlaylists(mPlaylists);
            playlistAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof PlaylistAdapter.ViewHolder) {
            PlaylistAdapter.ViewHolder selectedPlaylist = (PlaylistAdapter.ViewHolder) viewHolder;
            Playlist playlist = selectedPlaylist.mPlaylist;

            Fragment fragment = new TrackFragment();
            Bundle args = new Bundle();
            args.putParcelable(SELECTED_PLAYLIST, playlist);
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.mainContent, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
