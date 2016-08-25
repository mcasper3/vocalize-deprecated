package me.mikecasper.musicvoice.settings.voicecommands;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.mikecasper.musicvoice.R;

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

}
