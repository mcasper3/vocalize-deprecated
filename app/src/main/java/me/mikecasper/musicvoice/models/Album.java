package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Album implements Parcelable {
    private List<Image> images;
    private String name;
    private String uri;
    private String id;

    public Album(List<Image> images, String name, String uri, String id) {
        this.images = images;
        this.name = name;
        this.uri = uri;
        this.id = id;
    }

    public List<Image> getImages() {
        return images;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
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
        dest.writeString(uri);
        dest.writeString(id);
        dest.writeTypedList(images);
    }

    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    private Album(Parcel in) {
        this.name = in.readString();
        this.uri = in.readString();
        this.id = in.readString();
        this.images = in.createTypedArrayList(Image.CREATOR);
    }
}
