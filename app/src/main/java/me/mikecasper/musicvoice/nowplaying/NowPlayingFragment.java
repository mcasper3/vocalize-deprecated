package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import me.mikecasper.musicvoice.R;

public class NowPlayingFragment extends Fragment {

    private boolean mIsPlayingMusic;

    public NowPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);

        ImageView playPauseButton = (ImageView) view.findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePlayButton();
            }
        });

        return view;
    }

    private void updatePlayButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton();
        } else {
            View view = getView();

            if (view != null) {
                int id = mIsPlayingMusic ? R.drawable.ic_pause : R.drawable.ic_play;
                ImageView button = (ImageView) view.findViewById(R.id.playPauseButton);
                button.setImageResource(id);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton() {
        AnimatedVectorDrawable drawable;
        if (mIsPlayingMusic) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_pause_to_play);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_play_to_pause);
        }

        View view = getView();

        if (view != null) {
            ImageView button = (ImageView) view.findViewById(R.id.playPauseButton);
            button.setImageDrawable(drawable);
            drawable.start();
        }

        mIsPlayingMusic = !mIsPlayingMusic;
    }
}
