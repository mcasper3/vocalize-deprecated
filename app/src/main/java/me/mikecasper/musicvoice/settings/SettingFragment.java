package me.mikecasper.musicvoice.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.settings.libraries.LibrariesFragment;
import me.mikecasper.musicvoice.settings.voicecommands.VoiceCommandsFragment;
import me.mikecasper.musicvoice.views.DividerItemDecoration;

public class SettingFragment extends Fragment {

    public final static String LEFTIE_LAYOUT_SELECTED = "leftieLayoutSelected";

    private SettingRecyclerViewAdapter mRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        mRecyclerViewAdapter = new SettingRecyclerViewAdapter(this);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(context));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((MusicVoiceActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_settings);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.cleanUp();
            mRecyclerViewAdapter = null;
        }
    }

    public void onClick(int position) {
        if (position == 0) {
            // Start libraries fragment
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, new LibrariesFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (position == 1) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, new VoiceCommandsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
}
