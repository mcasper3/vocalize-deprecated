package me.mikecasper.musicvoice.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.settings.libraries.LibrariesActivity;

public class SettingsActivity extends MusicVoiceActivity {

    public static final String LEFTIE_LAYOUT_SELECTED = "leftieLayoutSelected";

    private static final int mColumnCount = 1;

    private RecyclerView mSettingsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSettingsList = (RecyclerView) findViewById(R.id.list);

        // Set the adapter
        if (mColumnCount <= 1) {
            mSettingsList.setLayoutManager(new LinearLayoutManager(mSettingsList.getContext()));
        } else {
            mSettingsList.setLayoutManager(new GridLayoutManager(mSettingsList.getContext(), mColumnCount));
        }
        mSettingsList.setAdapter(new SettingsRecyclerViewAdapter(this));
    }

    public void onClick(SettingsRecyclerViewAdapter.ViewHolder viewHolder, int position) {
        if (position == 0) {
            Intent intent = new Intent(this, LibrariesActivity.class);
            startActivity(intent);
        }
    }
}
