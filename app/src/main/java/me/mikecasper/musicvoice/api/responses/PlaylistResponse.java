package me.mikecasper.musicvoice.api.responses;

import java.util.List;

import me.mikecasper.musicvoice.models.Playlist;

public class PlaylistResponse {
    private List<Playlist> items;

    public PlaylistResponse(List<Playlist> playlists) {
        items = playlists;
    }

    public List<Playlist> getPlaylists() {
        return items;
    }
}
