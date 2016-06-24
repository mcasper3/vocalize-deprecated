package me.mikecasper.musicvoice.api.responses;

import android.os.Parcel;
import android.os.Parcelable;

import me.mikecasper.musicvoice.models.Track;

public class TrackResponseItem implements Parcelable {
    private Track track;

    public TrackResponseItem(Track track) {
        this.track = track;
    }

    public TrackResponseItem(TrackResponseItem item) {
        this.track = new Track(item.getTrack());
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        track.writeToParcel(dest, track.describeContents());
    }

    public static final Parcelable.Creator<TrackResponseItem> CREATOR = new Parcelable.Creator<TrackResponseItem>() {
        @Override
        public TrackResponseItem createFromParcel(Parcel source) {
            return new TrackResponseItem(source);
        }

        @Override
        public TrackResponseItem[] newArray(int size) {
            return new TrackResponseItem[size];
        }
    };

    private TrackResponseItem(Parcel in) {
        this.track = Track.CREATOR.createFromParcel(in);
    }
}
