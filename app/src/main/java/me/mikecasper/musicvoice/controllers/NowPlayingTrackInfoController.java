package me.mikecasper.musicvoice.controllers;

import android.os.Bundle;
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

    private IEventManager mEventManager;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;

    public NowPlayingTrackInfoController(View view, Track track) {
        mEventManager = EventManagerProvider.getInstance(view.getContext());

        setUpViews(view);

        if (track != null) {
            updateView(track);
        }
    }

    private void setUpViews(View view) {
        View queueButton = view.findViewById(R.id.queue_button);
        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new StartQueueFragmentEvent());
            }
        });

        mCurrentTime = (TextView) view.findViewById(R.id.current_time);
        mSeekBar = (SeekBar) view.findViewById(R.id.song_seek_bar);

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
            TextView trackName = (TextView) view.findViewById(R.id.track_name);
            if (trackName != null && !trackName.getText().toString().equals(track.getName())) {

                TextView currentTime = (TextView) view.findViewById(R.id.current_time);
                if (currentTime != null) {
                    currentTime.setText(R.string.initial_time);
                }

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
        // TODO
    }

    private void updateView(Track track) {
        mSeekBar.setMax(track.getDuration());

            TextView trackName = (TextView) view.findViewById(R.id.track_name);
            TextView artistName = (TextView) view.findViewById(R.id.artist_name);
            TextView remainingTime = (TextView) view.findViewById(R.id.remaining_time);

            if (trackName != null && artistName != null && remainingTime != null) {
                trackName.setText(track.getName());
                trackName.setSelected(true);
                trackName.setSingleLine(true);

                remainingTime.setText(DateUtility.formatDuration(track.getDuration()));

                String artistNames = "";

                for (Artist artist : track.getArtists()) {
                    artistNames += artist.getName() + ", ";
                }

                artistNames = artistNames.substring(0, artistNames.length() - 2);

                artistName.setText(artistNames);
                artistName.setSelected(true);
                artistName.setSingleLine(true);
            }
    }
}
