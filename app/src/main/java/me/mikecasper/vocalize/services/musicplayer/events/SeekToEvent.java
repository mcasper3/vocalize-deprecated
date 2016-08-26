package me.mikecasper.vocalize.services.musicplayer.events;

public class SeekToEvent {
    private int mPosition;

    public SeekToEvent(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }
}
