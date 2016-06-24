package me.mikecasper.musicvoice.track.events;

import me.mikecasper.musicvoice.api.responses.TrackResponse;

public class TracksObtainedEvent {

    private String mPlaylistId;
    private String mUserId;
    private TrackResponse mTrackResponse;

    public TracksObtainedEvent(String playlistId, String userId, TrackResponse trackResponse) {
        this.mPlaylistId = playlistId;
        this.mUserId = userId;
        this.mTrackResponse = trackResponse;
    }

    public String getPlaylistId() {
        return mPlaylistId;
    }

    public String getUserId() {
        return mUserId;
    }

    public TrackResponse getTrackResponse() {
        return mTrackResponse;
    }
}
