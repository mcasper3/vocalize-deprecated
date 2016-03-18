package me.mikecasper.musicvoice.models;

public class Image {
    private int height;
    private int width;
    private String url;

    public Image(int height, int width, String url) {
        this.height = height;
        this.width = width;
        this.url = url;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getUrl() {
        return url;
    }
}
