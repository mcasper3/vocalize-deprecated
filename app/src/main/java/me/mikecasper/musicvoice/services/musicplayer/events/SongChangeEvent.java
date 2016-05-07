package me.mikecasper.musicvoice.services.musicplayer.events;

import me.mikecasper.musicvoice.models.Track;

public class SongChangeEvent {
    private Track mTrack;

    public SongChangeEvent(Track track) {
        this.mTrack = track;
    }

    public Track getTrack() {
        return mTrack;
    }
}
