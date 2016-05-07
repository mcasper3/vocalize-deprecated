package me.mikecasper.musicvoice.api.responses;

import me.mikecasper.musicvoice.models.Track;

/**
 * Created by Mike on 3/14/2016.
 */
public class TrackResponseItem {
    private Track track;

    public TrackResponseItem(Track track) {
        this.track = track;
    }

    public TrackResponseItem(TrackResponseItem item) {
        this.track = new Track(item.getTrack());
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
