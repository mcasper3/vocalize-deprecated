package me.mikecasper.musicvoice.services.musicplayer.events;

import me.mikecasper.musicvoice.models.Track;

public class UpdatePlayerStatusEvent {
    private boolean mIsPlaying;
    private Track mTrack;

    public UpdatePlayerStatusEvent(boolean isPlaying, Track track) {
        mIsPlaying = isPlaying;
        mTrack = track;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public Track getTrack() {
        return mTrack;
    }
}
