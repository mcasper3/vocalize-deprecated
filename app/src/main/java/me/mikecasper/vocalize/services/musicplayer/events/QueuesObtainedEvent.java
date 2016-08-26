package me.mikecasper.vocalize.services.musicplayer.events;

import java.util.List;

import me.mikecasper.vocalize.models.Track;

public class QueuesObtainedEvent {
    private Track mNowPlaying;
    private List<Track> mQueue;
    private List<Track> mPriorityQueue;

    public QueuesObtainedEvent(Track nowPlaying, List<Track> queue, List<Track> priorityQueue) {
        this.mNowPlaying = nowPlaying;
        this.mQueue = queue;
        this.mPriorityQueue = priorityQueue;
    }

    public List<Track> getQueue() {
        return mQueue;
    }

    public List<Track> getPriorityQueue() {
        return mPriorityQueue;
    }

    public Track getNowPlaying() {
        return mNowPlaying;
    }
}
