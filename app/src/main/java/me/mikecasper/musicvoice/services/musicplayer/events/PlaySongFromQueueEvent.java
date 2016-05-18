package me.mikecasper.musicvoice.services.musicplayer.events;

public class PlaySongFromQueueEvent {
    private int mQueueIndex;
    private boolean mIsPriorityQueue;

    public PlaySongFromQueueEvent(int index, boolean isPriorityQueue) {
        mQueueIndex = index;
        mIsPriorityQueue = isPriorityQueue;
    }

    public int getQueueIndex() {
        return mQueueIndex;
    }

    public boolean isPriorityQueue() {
        return mIsPriorityQueue;
    }
}
