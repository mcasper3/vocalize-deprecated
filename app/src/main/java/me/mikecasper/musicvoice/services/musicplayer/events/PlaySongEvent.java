package me.mikecasper.musicvoice.services.musicplayer.events;

public class PlaySongEvent {
    private String mUri;

    public PlaySongEvent(String uri) {
        this.mUri = uri;
    }

    public String getUri() {
        return mUri;
    }
}
