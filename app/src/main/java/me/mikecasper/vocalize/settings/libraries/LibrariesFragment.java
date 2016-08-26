package me.mikecasper.vocalize.settings.libraries;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.mikecasper.vocalize.MusicVoiceActivity;
import me.mikecasper.vocalize.R;
import me.mikecasper.vocalize.views.DividerItemDecoration;

public class LibrariesFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LibrariesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_libraries_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new LibrariesRecyclerViewAdapter(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((MusicVoiceActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_libraries);
        }
    }

}
