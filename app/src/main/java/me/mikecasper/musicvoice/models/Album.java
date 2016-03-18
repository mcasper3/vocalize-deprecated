package me.mikecasper.musicvoice.models;

import java.util.List;

public class Album {
    private List<Image> images;
    private String name;
    private String uri;
    private String id;

    public Album(List<Image> images, String name, String uri, String id) {
        this.images = images;
        this.name = name;
        this.uri = uri;
        this.id = id;
    }

    public List<Image> getImages() {
        return images;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }
}
