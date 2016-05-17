package me.mikecasper.musicvoice.services.musicplayer.events;

import me.mikecasper.musicvoice.models.Track;

public class UpdatePlayerStatusEvent {
    private boolean mIsPlaying;
    private Track mTrack;
    private int mCurrentSongPosition;

    public UpdatePlayerStatusEvent(boolean isPlaying, Track track, int currentSongPosition) {
        mIsPlaying = isPlaying;
        mTrack = track;
        mCurrentSongPosition = currentSongPosition;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public Track getTrack() {
        return mTrack;
    }

    public int getCurrentSongPosition() {
        return mCurrentSongPosition;
    }
}
