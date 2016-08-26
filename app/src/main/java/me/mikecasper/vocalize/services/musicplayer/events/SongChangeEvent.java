package me.mikecasper.vocalize.services.musicplayer.events;

import me.mikecasper.vocalize.models.Track;

public class SongChangeEvent {
    private Track mTrack;
    private boolean mPlayingSong;

    public SongChangeEvent(Track track, boolean playingSong) {
        this.mTrack = track;
        this.mPlayingSong = playingSong;
    }

    public Track getTrack() {
        return mTrack;
    }

    public boolean isPlayingSong() {
        return mPlayingSong;
    }
}
