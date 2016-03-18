package me.mikecasper.musicvoice.models;

import java.util.List;

public class Playlist {
    private String uri;
    private String name;
    private String id;
    private List<Track> items;
    private List<Image> images;
    private Track tracks;

    public Playlist(String uri, String name, String id, List<Track> items, List<Image> images, Track tracks) {
        this.uri = uri;
        this.name = name;
        this.id = id;
        this.items = items;
        this.images = images;
        this.tracks = tracks;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Track> getItems() {
        return items;
    }

    public List<Image> getImages() {
        return images;
    }

    public Track getTracks() {
        return tracks;
    }
}
