package me.mikecasper.musicvoice.track;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManager;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.util.RecyclerViewItemClickListener;

import java.util.List;

public class TrackFragment extends Fragment implements RecyclerViewItemClickListener {

    private List<Track> mTracks;
    private EventManager mEventManager;

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

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new TrackAdapter(mTracks, this));
        }

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
    }

    @Subscribe
    public void onTracksObtained() {

    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof TrackAdapter.ViewHolder) {
            TrackAdapter.ViewHolder selectedTrack = (TrackAdapter.ViewHolder) viewHolder;

            // TODO play track
        }
    }
}
