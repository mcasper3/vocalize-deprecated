package me.mikecasper.musicvoice.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.nowplaying.events.StartQueueFragmentEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.StopSeekbarUpdateEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.util.DateUtility;

public class NowPlayingTrackInfoController implements MusicInfoController {

    private Context mContext;
    private IEventManager mEventManager;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mTrackName;
    private TextView mArtistName;
    private TextView mRemainingTime;

    public NowPlayingTrackInfoController(View view, Bundle args) {
        mContext = view.getContext();
        mEventManager = EventManagerProvider.getInstance(view.getContext());

        Track track = args.getParcelable(NowPlayingActivity.TRACK);

        setUpViews(view, args);

        if (track != null) {
            updateView(track);
        }
    }

    private void setUpViews(View view, Bundle args) {
        TextView playlistName = (TextView) view.findViewById(R.id.playlist_name);
        playlistName.setSelected(true);
        playlistName.setSingleLine(true);

        String playlist = args.getString(NowPlayingActivity.PLAYLIST_NAME, null);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (playlist == null) {
            playlist = sharedPreferences.getString(NowPlayingActivity.PLAYLIST_NAME, null);
        } else {
            sharedPreferences.edit()
                    .putString(NowPlayingActivity.PLAYLIST_NAME, playlist)
                    .apply();
        }

        playlistName.setText(mContext.getString(R.string.playing_from, playlist));

        View queueButton = view.findViewById(R.id.queue_button);
        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new StartQueueFragmentEvent());
            }
        });

        mCurrentTime = (TextView) view.findViewById(R.id.current_time);
        mSeekBar = (SeekBar) view.findViewById(R.id.song_seek_bar);
        mTrackName = (TextView) view.findViewById(R.id.track_name);
        mArtistName = (TextView) view.findViewById(R.id.artist_name);
        mRemainingTime = (TextView) view.findViewById(R.id.remaining_time);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCurrentTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mEventManager.postEvent(new StopSeekbarUpdateEvent());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mEventManager.postEvent(new SeekToEvent(seekBar.getProgress()));
            }
        });

        int currentTime = args.getInt(NowPlayingActivity.CURRENT_TIME, 0);
        mSeekBar.setProgress(currentTime);
    }

    private void updateCurrentTime(int position) {
        String time = DateUtility.formatDuration(position);
        mCurrentTime.setText(time);
    }

    @Subscribe
    @Override
    public void onSongTimeUpdated(UpdateSongTimeEvent event) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(event.getSongTime());
        }
    }

    @Subscribe
    @Override
    public void onSongChange(SongChangeEvent event) {
        Track track = event.getTrack();

        if (track != null) {
            if (!mTrackName.getText().toString().equals(track.getName())) {

                mCurrentTime.setText(R.string.initial_time);

                updateView(track);
            }
        }
    }

    @Override
    public void onPlayerStatusObtained(UpdatePlayerStatusEvent event) {
        Track track = event.getTrack();

        if (track != null) {
            updateView(track);

            if (mSeekBar != null) {
                mSeekBar.setProgress(event.getCurrentSongPosition());
            }
        }
    }

    @Override
    public void tearDown() {
        mArtistName = null;
        mRemainingTime = null;
        mTrackName = null;
        mCurrentTime = null;
        mSeekBar = null;
        mContext = null;
        mEventManager = null;
    }

    private void updateView(Track track) {
        mSeekBar.setMax(track.getDuration());

        mTrackName.setText(track.getName());
        mTrackName.setSelected(true);
        mTrackName.setSingleLine(true);

        mRemainingTime.setText(DateUtility.formatDuration(track.getDuration()));

        String artistNames = "";

        for (Artist artist : track.getArtists()) {
            artistNames += artist.getName() + ", ";
        }

        artistNames = artistNames.substring(0, artistNames.length() - 2);

        mArtistName.setText(artistNames);
        mArtistName.setSelected(true);
        mArtistName.setSingleLine(true);
    }
}
