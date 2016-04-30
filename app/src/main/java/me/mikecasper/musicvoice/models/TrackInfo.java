package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackInfo implements Parcelable {

    private int total;
    private String href;

    public TrackInfo(int total, String href) {
        this.total = total;
        this.href = href;
    }

    public int getTotal() {
        return total;
    }

    public String getHref() {
        return href;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(total);
        dest.writeString(href);
    }

    public static final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable.Creator<TrackInfo>() {
        @Override
        public TrackInfo createFromParcel(Parcel source) {
            return new TrackInfo(source);
        }

        @Override
        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };

    private TrackInfo(Parcel in) {
        this.total = in.readInt();
        this.href = in.readString();
    }
}
