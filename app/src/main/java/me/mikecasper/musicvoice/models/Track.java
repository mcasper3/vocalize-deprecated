package me.mikecasper.musicvoice.models;

import java.util.List;

/**
 * Created by Mike on 3/14/2016.
 */
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
}
