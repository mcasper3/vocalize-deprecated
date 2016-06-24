package me.mikecasper.musicvoice.nowplaying.events;

import java.util.List;

import me.mikecasper.musicvoice.nowplaying.models.QueueItemInformation;

public class AddTracksToPriorityQueueEvent {
    private List<QueueItemInformation> mTracksToAdd;

    public List<QueueItemInformation> getTracksToAdd() {
        return mTracksToAdd;
    }

    public AddTracksToPriorityQueueEvent(List<QueueItemInformation> mTracksToAdd) {
        this.mTracksToAdd = mTracksToAdd;
    }
}
