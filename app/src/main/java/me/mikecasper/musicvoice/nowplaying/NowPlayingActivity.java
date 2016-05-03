package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.track.TrackFragment;

public class NowPlayingActivity extends MusicVoiceActivity {

    // Services
    private IEventManager mEventManager;

    // Data Members
    private boolean mIsPlayingMusic;

    public NowPlayingActivity() {
        // Required empty public constructor
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        mEventManager = EventManagerProvider.getInstance(this);

        ImageView playPauseButton = (ImageView) findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePlayButton();
            }
        });

        ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        TextView trackName = (TextView) findViewById(R.id.trackName);
        TextView artistName = (TextView) findViewById(R.id.artistName);
        TextView currentTime = (TextView) findViewById(R.id.currentTime);
        TextView remainingTime = (TextView) findViewById(R.id.remainingTime);

        Intent intent = getIntent();
        Track track = intent.getParcelableExtra(TrackFragment.TRACK);

        if (track != null) {
            Picasso.with(this)
                    .load(track.getAlbum().getImages().get(0).getUrl())
                    .placeholder(R.drawable.default_playlist)
                    .error(R.drawable.default_playlist)
                    .into(albumArt);

            trackName.setText(track.getName());
            artistName.setText(track.getArtists().get(0).getName());
        }
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

    private void updatePlayButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton();
        } else {
            int id = mIsPlayingMusic ? R.drawable.ic_pause : R.drawable.ic_play;
            ImageView button = (ImageView) findViewById(R.id.playPauseButton);

            if (button != null) {
                button.setImageResource(id);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton() {
        AnimatedVectorDrawable drawable;
        if (mIsPlayingMusic) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause);
        }

        ImageView button = (ImageView) findViewById(R.id.playPauseButton);

        if (button != null) {
            button.setImageDrawable(drawable);
            drawable.start();
        }

        mIsPlayingMusic = !mIsPlayingMusic;
    }
}
