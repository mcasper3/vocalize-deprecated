package me.mikecasper.musicvoice.services.musicplayer.events;

import java.util.List;

import me.mikecasper.musicvoice.models.Track;

public class OnQueuesObtainedEvent {
    private List<Track> mQueue;
    private List<Track> mPriorityQueue;

    public OnQueuesObtainedEvent(List<Track> queue, List<Track> priorityQueue) {
        this.mQueue = queue;
        this.mPriorityQueue = priorityQueue;
    }

    public List<Track> getQueue() {
        return mQueue;
    }

    public List<Track> getPriorityQueue() {
        return mPriorityQueue;
    }
}
