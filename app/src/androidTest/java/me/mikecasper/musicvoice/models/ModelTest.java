package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ModelTest {

    @Test
    public void image_ParcelableWriteRead() {
        Image image = new Image(1, 2, "3");

        Parcel parcel = Parcel.obtain();
        image.writeToParcel(parcel, image.describeContents());

        parcel.setDataPosition(0);

        Image createdFromParcel = Image.CREATOR.createFromParcel(parcel);

        assertThat(createdFromParcel.getHeight(), is(1));
        assertThat(createdFromParcel.getWidth(), is(2));
        assertThat(createdFromParcel.getUrl(), is("3"));
    }

    @Test
    public void track_ParcelableWriteRead() {
        List<Image> images = new ArrayList<>();
        images.add(new Image(1, 2, "3"));
        Album album = new Album(images, "album1", "uri", "1");
        List<Artist> artists = new ArrayList<>();
        artists.add(new Artist("1", "artist1"));
        Track track = new Track(1000, "uri", "track1", true, album, artists, 1);

        Parcel parcel = Parcel.obtain();
        track.writeToParcel(parcel, track.describeContents());

        parcel.setDataPosition(0);

        Track createdFromParcel = Track.CREATOR.createFromParcel(parcel);

        Artist artistFromParcel = createdFromParcel.getArtists().get(0);
        Album albumFromParcel = createdFromParcel.getAlbum();
        Image imageFromParcel = albumFromParcel.getImages().get(0);

        assertThat(albumFromParcel.getName(), is("album1"));
        assertThat(imageFromParcel.getUrl(), is("3"));
        assertThat(artistFromParcel.getName(), is("artist1"));
        assertThat(createdFromParcel.getName(), is("track1"));
        assertThat(createdFromParcel.getDuration(), is(1000));
        assertThat(createdFromParcel.getUri(), is("uri"));
        assertThat(createdFromParcel.isPlayable(), is(true));
        assertThat(createdFromParcel.getTotal(), is(1));
    }

    @Test
    public void album_ParcelableWriteRead() {
        List<Image> images = new ArrayList<>();
        images.add(new Image(1, 2, "3"));
        Album album = new Album(images, "album1", "uri", "1");

        Parcel parcel = Parcel.obtain();
        album.writeToParcel(parcel, album.describeContents());

        parcel.setDataPosition(0);

        Album createdFromParcel = Album.CREATOR.createFromParcel(parcel);

        Image imageFromParcel = createdFromParcel.getImages().get(0);

        assertThat(imageFromParcel.getUrl(), is("3"));
        assertThat(createdFromParcel.getName(), is("album1"));
        assertThat(createdFromParcel.getUri(), is("uri"));
        assertThat(createdFromParcel.getId(), is("1"));
    }

    @Test
    public void playlist_ParcelableWriteRead() {
        List<Image> images = new ArrayList<>();
        images.add(new Image(1, 2, "3"));
        Album album = new Album(images, "album1", "uri", "1");
        List<Artist> artists = new ArrayList<>();
        artists.add(new Artist("1", "artist1"));
        Track track = new Track(1000, "uri", "track1", true, album, artists, 1);
        SpotifyUser owner = new SpotifyUser("1", "uri", images, "display");
        Playlist playlist = new Playlist("uri", "playlist1", "1", images, track, owner);

        Parcel parcel = Parcel.obtain();
        playlist.writeToParcel(parcel, playlist.describeContents());

        parcel.setDataPosition(0);

        Playlist createdFromParcel = Playlist.CREATOR.createFromParcel(parcel);

        Image imageFromParcel = createdFromParcel.getImages().get(0);
        Track trackFromParcel = createdFromParcel.getTracks();
        SpotifyUser ownerFromParcel = createdFromParcel.getOwner();

        assertThat(imageFromParcel.getUrl(), is("3"));
        assertThat(trackFromParcel.getName(), is("track1"));
        assertThat(ownerFromParcel.getDisplay_name(), is("display"));
        assertThat(createdFromParcel.getName(), is("playlist1"));
        assertThat(createdFromParcel.getUri(), is("uri"));
        assertThat(createdFromParcel.getId(), is("1"));
    }
}
