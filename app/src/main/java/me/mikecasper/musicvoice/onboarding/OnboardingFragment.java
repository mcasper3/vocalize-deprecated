package me.mikecasper.musicvoice.onboarding;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import me.mikecasper.musicvoice.R;

public class OnboardingFragment extends Fragment {

    public static final String BACKGROUND_COLOR = "backgroundColor";
    public static final String IMAGE_ID = "imageId";
    public static final String TEXT_ID = "textId";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        int color = args.getInt(BACKGROUND_COLOR, R.color.first_onboarding_view);

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.onboarding_fragment_parent);
        parent.setBackgroundColor(ContextCompat.getColor(getContext(), color));
    }
}
