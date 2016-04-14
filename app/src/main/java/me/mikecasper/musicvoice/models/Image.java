package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    private int height;
    private int width;
    private String url;

    public Image(int height, int width, String url) {
        this.height = height;
        this.width = width;
        this.url = url;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeString(url);
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    private Image(Parcel in) {
        this.height = in.readInt();
        this.width = in.readInt();
        this.url = in.readString();
    }
}
