package me.mikecasper.musicvoice.services.musicplayer.events;

import java.util.List;

import me.mikecasper.musicvoice.api.responses.TrackResponseItem;

public class SetPlaylistEvent {
    private List<TrackResponseItem> mTracks;
    private int mPosition;

    public SetPlaylistEvent(List<TrackResponseItem> tracks, int position) {
        mTracks = tracks;
        mPosition = position;
    }

    public List<TrackResponseItem> getTracks() {
        return mTracks;
    }

    public int getPosition() {
        return mPosition;
    }
}
