package me.mikecasper.musicvoice.playlist.events;

public class GetPlaylistTracksEvent {
    private final String mUserId;
    private final String mPlaylistId;
    private final int mOffset;

    public GetPlaylistTracksEvent(String userId, String playlistId, int offset) {
        this.mUserId = userId;
        this.mPlaylistId = playlistId;
        this.mOffset = offset;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getPlaylistId() {
        return mPlaylistId;
    }

    public int getOffset() {
        return mOffset;
    }
}
