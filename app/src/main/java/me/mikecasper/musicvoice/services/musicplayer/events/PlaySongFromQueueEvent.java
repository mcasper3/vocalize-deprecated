package me.mikecasper.musicvoice.services.musicplayer.events;

public class PlaySongFromQueueEvent {
    private int mQueueIndex;

    public PlaySongFromQueueEvent(int index) {
        mQueueIndex = index;
    }

    public int getQueueIndex() {
        return mQueueIndex;
    }
}
