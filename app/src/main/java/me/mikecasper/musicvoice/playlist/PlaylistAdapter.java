package me.mikecasper.musicvoice.playlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Playlist;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private List<Playlist> mPlaylists;
    private Context mContext;

    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        mPlaylists = playlists;
        mContext = context;
    }

    public void setPlaylists(List<Playlist> playlists) {
        mPlaylists = playlists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View playlistView = LayoutInflater.from(context).inflate(R.layout.layout_playlist_item, parent, false);

        return new ViewHolder(playlistView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Playlist playlist = mPlaylists.get(position);

        Picasso.with(mContext).load(playlist.getImages().get(0).getUrl()).placeholder(R.drawable.default_playlist).into(holder.playlistArt);

        holder.playlistTitle.setText(playlist.getName());
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView playlistArt;
        public TextView playlistTitle;

        public ViewHolder(View itemView) {
            super(itemView);

            playlistArt = (ImageView) itemView.findViewById(R.id.playlistArt);
            playlistTitle = (TextView) itemView.findViewById(R.id.playlistTitle);
        }
    }
}
