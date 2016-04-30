package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class SpotifyUser implements Parcelable {

    public static final String NAME = "spotifyUserName";
    public static final String PROFILE_IMAGE = "spotifyUserProfileImage";
    public static final String ID = "spotifyUserId";

    private String id;
    private String uri;
    private List<Image> images;
    private String display_name;

    public SpotifyUser(String id, String uri, List<Image> images, String display_name) {
        this.id = id;
        this.uri = uri;
        this.images = images;
        this.display_name = display_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeString(display_name);
        dest.writeString(id);
        dest.writeTypedList(images);
    }

    public static final Parcelable.Creator<SpotifyUser> CREATOR = new Parcelable.Creator<SpotifyUser>() {
        @Override
        public SpotifyUser createFromParcel(Parcel source) {
            return new SpotifyUser(source);
        }

        @Override
        public SpotifyUser[] newArray(int size) {
            return new SpotifyUser[size];
        }
    };

    private SpotifyUser(Parcel in) {
        this.uri = in.readString();
        this.display_name = in.readString();
        this.id = in.readString();
        this.images = in.createTypedArrayList(Image.CREATOR);
    }
}
