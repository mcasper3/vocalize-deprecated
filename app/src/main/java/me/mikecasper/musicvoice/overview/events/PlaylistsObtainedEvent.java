package me.mikecasper.musicvoice.overview.events;

import java.util.List;

import me.mikecasper.musicvoice.models.Playlist;

public class PlaylistsObtainedEvent {

    private List<Playlist> mPlaylists;

    public PlaylistsObtainedEvent(List<Playlist> playlists) {
        mPlaylists = playlists;
    }

    public List<Playlist> getPlaylists() {
        return mPlaylists;
    }
}
