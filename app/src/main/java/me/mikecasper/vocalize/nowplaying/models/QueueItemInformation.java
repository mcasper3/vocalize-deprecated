package me.mikecasper.vocalize.nowplaying.models;

public class QueueItemInformation {
    private int mTrackIndex;
    private boolean mFromPriorityQueue;

    public QueueItemInformation(int mTrackIndex, boolean mFromPriorityQueue) {
        this.mTrackIndex = mTrackIndex;
        this.mFromPriorityQueue = mFromPriorityQueue;
    }

    public int getTrackIndex() {
        return mTrackIndex;
    }

    public boolean isFromPriorityQueue() {
        return mFromPriorityQueue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!QueueItemInformation.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final QueueItemInformation other = (QueueItemInformation) o;

        return other.mFromPriorityQueue == this.mFromPriorityQueue && other.mTrackIndex == this.mTrackIndex;

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.mTrackIndex;
        hash = 53 * hash + (this.mFromPriorityQueue ? 1 : 0);
        return hash;
    }
}
