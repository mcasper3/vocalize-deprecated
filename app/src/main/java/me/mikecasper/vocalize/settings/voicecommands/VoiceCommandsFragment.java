package me.mikecasper.vocalize.settings.voicecommands;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.mikecasper.vocalize.MusicVoiceActivity;
import me.mikecasper.vocalize.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class VoiceCommandsFragment extends Fragment {

    public VoiceCommandsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_voice_commands, container, false);

        TextView voiceCommandsView = (TextView) view.findViewById(R.id.voice_commands);

        String[] voiceCommands = getResources().getStringArray(R.array.voice_commands);

        StringBuilder voiceCommandList = new StringBuilder();
        for (String command : voiceCommands) {
            voiceCommandList.append("\n");
            voiceCommandList.append(command);
        }
        voiceCommandsView.setText(voiceCommandList.toString());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((MusicVoiceActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_voice_commands);
        }
    }

}
