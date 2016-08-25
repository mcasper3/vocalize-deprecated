package me.mikecasper.musicvoice.controllers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.settings.SettingFragment;

public class MainMusicButtonController implements MusicButtonsController, MusicInfoController {

    private View mView;
    private Context mContext;
    private ProgressBar mProgressBar;
    private ImageView mLeftImage;
    private ImageView mRightImage;
    private TextView mTrackName;
    private TextView mArtistName;
    private boolean mIsPlaying;
    private Track mTrack;
    private boolean mLeftieLayout;
    private IEventManager mEventManager;

    public MainMusicButtonController(View view) {
        mView = view;
        mContext = mView.getContext();
        mProgressBar = (ProgressBar) mView.findViewById(R.id.mini_song_time);
        mEventManager = EventManagerProvider.getInstance(mContext);

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMusicButtonController.this.onClick();
            }
        });

        setUpView();
    }

    private void setUpView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLeftieLayout = sharedPreferences.getBoolean(SettingFragment.LEFTIE_LAYOUT_SELECTED, false);

        mLeftImage = (ImageView) mView.findViewById(R.id.left_image);
        mRightImage = (ImageView) mView.findViewById(R.id.right_image);
        mTrackName = (TextView) mView.findViewById(R.id.mini_track_name);
        mArtistName = (TextView) mView.findViewById(R.id.mini_artist_name);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPlaying = !mIsPlaying;
                mEventManager.postEvent(new TogglePlaybackEvent());

                updatePlayButton((ImageView) v);
            }
        };

        if (mLeftieLayout) {
            mLeftImage.setImageResource(R.drawable.ic_play);
            mRightImage.setImageResource(R.drawable.default_playlist);

            mLeftImage.setOnClickListener(onClickListener);
        } else {
            mLeftImage.setImageResource(R.drawable.default_playlist);
            mRightImage.setImageResource(R.drawable.ic_play);

            mRightImage.setOnClickListener(onClickListener);
        }
    }

    public void setContext(Context context) {
        mContext = context;
    }

    // region MusicButtonsController

    @Subscribe
    @Override
    public void onPlayerStatusObtained(UpdatePlayerStatusEvent event) {
        mIsPlaying = event.isPlaying();
        mTrack = event.getTrack();

        if (mIsPlaying || mTrack != null) {
            if (mTrack != null) {
                updateView();
            }

            mProgressBar.setProgress(event.getCurrentSongPosition());

            // display the view if it is hidden
            if (mView.getVisibility() == View.GONE) {
                Animation slideUp = AnimationUtils.loadAnimation(mView.getContext(), R.anim.slide_up);

                mView.setVisibility(View.VISIBLE);
                mView.startAnimation(slideUp);
            }
        } else {
            mView.setVisibility(View.GONE);
        }
    }

    @Subscribe
    @Override
    public void onSongChange(SongChangeEvent event) {
        mIsPlaying = event.isPlayingSong();
        mTrack = event.getTrack();

        updateView();
    }

    @Subscribe
    @Override
    public void onLostPermission(LostPermissionEvent event) {
        if (mIsPlaying) {
            mIsPlaying = false;
            updateView();
        }
    }

    private void updateView() {
        mProgressBar.setMax(mTrack.getDuration());

        int drawableId = mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play;

        if (mLeftieLayout) {
            mLeftImage.setImageResource(drawableId);
            Picasso.with(mContext).load(mTrack.getAlbum().getImages().get(0).getUrl()).into(mRightImage);
        } else {
            Picasso.with(mContext).load(mTrack.getAlbum().getImages().get(0).getUrl()).into(mLeftImage);
            mRightImage.setImageResource(drawableId);
        }

        mTrackName.setText(mTrack.getName());

        String artistNames = "";

        for (Artist artist : mTrack.getArtists()) {
            artistNames += artist.getName() + ", ";
        }

        artistNames = artistNames.substring(0, artistNames.length() - 2);
        mArtistName.setText(artistNames);
    }

    private void updatePlayButton(ImageView imageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton(imageView);
        } else {
            int id = mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play;

            imageView.setImageResource(id);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton(ImageView imageView) {
        AnimatedVectorDrawable drawable;
        if (mIsPlaying) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(mContext, R.drawable.avd_play_to_pause);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(mContext, R.drawable.avd_pause_to_play);
        }

        imageView.setImageDrawable(drawable);
        drawable.start();
    }

    // endregion MusicButtonsController

    // region MusicInfoController

    @Subscribe
    @Override
    public void onSongTimeUpdated(UpdateSongTimeEvent event) {
        mProgressBar.setProgress(event.getSongTime());
    }

    // endregion MusicInfoController

    private void onClick() {
        Intent intent = new Intent(mContext, NowPlayingActivity.class);
        intent.putExtra(NowPlayingActivity.TRACK, mTrack);
        intent.putExtra(NowPlayingActivity.IS_PLAYING_MUSIC, mIsPlaying);
        intent.putExtra(NowPlayingActivity.CURRENT_TIME, mProgressBar.getProgress());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent);
    }

    @Override
    public void tearDown() {
        mTrack = null;
        mProgressBar = null;

        // TODO clean up track image
    }
}
