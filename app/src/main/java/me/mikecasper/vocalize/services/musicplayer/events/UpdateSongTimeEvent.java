package me.mikecasper.vocalize.services.musicplayer.events;

public class UpdateSongTimeEvent {
    private int mSongTime;

    public UpdateSongTimeEvent(int songTime) {
        mSongTime = songTime;
    }

    public int getSongTime() {
        return mSongTime;
    }
}
