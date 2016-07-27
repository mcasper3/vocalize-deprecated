package me.mikecasper.musicvoice.services.musicplayer.events;

public class ToggleShuffleEvent {
    private boolean mOverrideShuffle;

    public ToggleShuffleEvent() {
        this.mOverrideShuffle = false;
    }

    public ToggleShuffleEvent(boolean overrideShuffle) {
        this.mOverrideShuffle = overrideShuffle;
    }

    public boolean shouldOverrideShuffle() {
        return mOverrideShuffle;
    }
}
