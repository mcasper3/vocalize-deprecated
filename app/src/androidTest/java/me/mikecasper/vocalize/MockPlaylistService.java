package me.mikecasper.vocalize;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.mikecasper.vocalize.api.responses.PlaylistResponse;
import me.mikecasper.vocalize.models.Playlist;
import me.mikecasper.vocalize.playlist.events.GetPlaylistsEvent;

public class MockPlaylistService {

    private Bus mBus;

    public MockPlaylistService(Bus bus) {
        mBus = bus;
    }

    @Subscribe
    public void onGetPlaylists(GetPlaylistsEvent event) {
        List<Playlist> playlists = new ArrayList<>();

        playlists.add(new Playlist("uri1", "playlist1", "1", null, null, null));
        playlists.add(new Playlist("uri1", "playlist1", "1", null, null, null));
        playlists.add(new Playlist("uri1", "playlist1", "1", null, null, null));
        playlists.add(new Playlist("uri1", "playlist1", "1", null, null, null));
        playlists.add(new Playlist("uri1", "playlist1", "1", null, null, null));

        mBus.post(new PlaylistResponse(playlists));
    }
}
