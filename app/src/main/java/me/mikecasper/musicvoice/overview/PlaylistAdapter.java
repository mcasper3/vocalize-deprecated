package me.mikecasper.musicvoice.overview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Playlist;

public class PlaylistAdapter extends BaseAdapter {

    private List<Playlist> mPlaylists;
    private Context mContext;

    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        mContext = context;
        mPlaylists = playlists;
    }

    @Override
    public int getCount() {
        return mPlaylists.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlaylists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Playlist playlist = mPlaylists.get(position);

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_playlist_item, parent, false);
        }

        ImageView playlistArt = (ImageView) view.findViewById(R.id.playlistArt);
        if (playlist.getImages().size() > 0) {
            Picasso.with(mContext).load(playlist.getImages().get(0).getUrl()).into(playlistArt);
        } else {
            // TODO default image
        }

        TextView albumTitle = (TextView) view.findViewById(R.id.playlistTitle);
        albumTitle.setText(playlist.getName());

        return view;
    }
}
