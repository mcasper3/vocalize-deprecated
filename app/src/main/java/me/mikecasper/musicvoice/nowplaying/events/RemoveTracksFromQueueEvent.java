package me.mikecasper.musicvoice.nowplaying.events;

import java.util.List;

import me.mikecasper.musicvoice.nowplaying.models.QueueItemInformation;

public class RemoveTracksFromQueueEvent {
    private List<QueueItemInformation> mTracksToRemove;

    public RemoveTracksFromQueueEvent(List<QueueItemInformation> mTracksToRemove) {
        this.mTracksToRemove = mTracksToRemove;
    }

    public List<QueueItemInformation> getTracksToRemove() {
        return mTracksToRemove;
    }
}
