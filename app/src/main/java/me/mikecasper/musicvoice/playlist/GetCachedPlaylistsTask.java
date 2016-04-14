package me.mikecasper.musicvoice.playlist;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import me.mikecasper.musicvoice.models.Playlist;

public class GetCachedPlaylistsTask extends AsyncTask<Context, Void, List<Playlist>> {
    @Override
    protected List<Playlist> doInBackground(Context... params) {
        List playlists = null;



        return playlists;
    }

    @Override
    protected void onPostExecute(List<Playlist> playlists) {
        super.onPostExecute(playlists);
    }
}
