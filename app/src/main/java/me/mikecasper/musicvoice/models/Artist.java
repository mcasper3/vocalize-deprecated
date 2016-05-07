package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable {
    private String id;
    private String name;

    public Artist(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Artist(Artist artist) {
        this.id = artist.getId();
        this.name = artist.getName();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    private Artist(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
    }
}
