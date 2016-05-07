package me.mikecasper.musicvoice.services.musicplayer.events;

public class SeekToEvent {
    private int mPosition;

    public SeekToEvent(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }
}
