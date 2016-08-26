package me.mikecasper.vocalize.services.musicplayer.events;

import me.mikecasper.vocalize.models.Track;

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
