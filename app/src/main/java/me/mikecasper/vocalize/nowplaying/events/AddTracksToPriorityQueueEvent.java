package me.mikecasper.vocalize.nowplaying.events;

import java.util.List;

import me.mikecasper.vocalize.nowplaying.models.QueueItemInformation;

public class AddTracksToPriorityQueueEvent {
    private List<QueueItemInformation> mTracksToAdd;

    public List<QueueItemInformation> getTracksToAdd() {
        return mTracksToAdd;
    }

    public AddTracksToPriorityQueueEvent(List<QueueItemInformation> mTracksToAdd) {
        this.mTracksToAdd = mTracksToAdd;
    }
}
