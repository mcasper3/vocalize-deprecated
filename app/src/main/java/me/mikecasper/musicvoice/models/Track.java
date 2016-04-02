package me.mikecasper.musicvoice.models;

import java.util.List;

public class Track {
    private int duration_ms;
    private String uri;
    private String name;
    private boolean is_playable;
    private Album album;
    private List<Artist> artists;
    private int total;

    public Track(int duration_ms, String uri, String name, boolean is_playable, Album album, List<Artist> artists, int total) {
        this.duration_ms = duration_ms;
        this.uri = uri;
        this.name = name;
        this.is_playable = is_playable;
        this.album = album;
        this.artists = artists;
        this.total = total;
    }

    public int getDuration() {
        return duration_ms;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public boolean isPlayable() {
        return is_playable;
    }

    public Album getAlbum() {
        return album;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public int getTotal() {
        return total;
    }
}
