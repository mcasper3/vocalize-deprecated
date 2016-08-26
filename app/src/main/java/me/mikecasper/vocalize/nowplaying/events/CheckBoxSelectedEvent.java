package me.mikecasper.vocalize.nowplaying.events;

import me.mikecasper.vocalize.nowplaying.models.QueueItemInformation;

public class CheckBoxSelectedEvent {
    private QueueItemInformation mItemInformation;
    private boolean mIsSelected;

    public CheckBoxSelectedEvent(QueueItemInformation itemInformation, boolean isSelected) {
        this.mItemInformation = itemInformation;
        this.mIsSelected = isSelected;
    }

    public QueueItemInformation getItemInformation() {
        return mItemInformation;
    }

    public boolean isSelected() {
        return mIsSelected;
    }
}
