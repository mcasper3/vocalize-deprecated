package me.mikecasper.musicvoice.onboarding;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.onboarding.events.ScrollLeftEvent;
import me.mikecasper.musicvoice.onboarding.events.ScrollRightEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.settings.SettingsFragment;

public class OnboardingFragment extends Fragment {

    public static final String BACKGROUND_COLOR = "backgroundColor";
    public static final String IMAGE_ID = "imageId";
    public static final String TEXT_ID = "textId";
    public static final String LEFT_OPTION = "leftOption";
    public static final String RIGHT_OPTION = "rightOption";

    private IEventManager mEventManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventManager = EventManagerProvider.getInstance(getContext());

        Bundle args = getArguments();
        int color = args.getInt(BACKGROUND_COLOR, R.color.first_onboarding_view);

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.onboarding_fragment_parent);
        parent.setBackgroundColor(ContextCompat.getColor(getContext(), color));

        Button leftOption = (Button) view.findViewById(R.id.first_option);
        Button rightOption = (Button) view.findViewById(R.id.second_option);

        int leftOptionText = args.getInt(LEFT_OPTION, R.string.back);
        int rightOptionText = args.getInt(RIGHT_OPTION, R.string.next);

        leftOption.setText(leftOptionText);
        rightOption.setText(rightOptionText);

        if (leftOptionText == R.string.left) {
            leftOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateViewOption(true);
                    mEventManager.postEvent(new ScrollRightEvent());
                }
            });

            rightOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateViewOption(false);
                    mEventManager.postEvent(new ScrollRightEvent());
                }
            });
        } else {
            leftOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEventManager.postEvent(new ScrollLeftEvent());
                }
            });

            rightOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEventManager.postEvent(new ScrollRightEvent());
                }
            });
        }
    }

    private void updateViewOption(boolean leftieLayout) {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putBoolean(SettingsFragment.LEFTIE_LAYOUT_SELECTED, leftieLayout)
                .apply();
    }

    @Override
    public void onDestroy() {
        mEventManager = null;

        ((MusicVoiceApplication) getActivity().getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }
}
