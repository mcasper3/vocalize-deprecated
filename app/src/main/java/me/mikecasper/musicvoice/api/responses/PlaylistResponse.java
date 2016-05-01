package me.mikecasper.musicvoice.api.responses;

import java.util.List;

import me.mikecasper.musicvoice.models.Playlist;

public class PlaylistResponse {
    private List<Playlist> items;
    private String next;
    private String previous;
    private int offset;
    private int limit;
    private int total;

    public PlaylistResponse(List<Playlist> playlists) {
        items = playlists;
    }

    public List<Playlist> getPlaylists() {
        return items;
    }

    public String getNext() {
        return next;
    }
}
