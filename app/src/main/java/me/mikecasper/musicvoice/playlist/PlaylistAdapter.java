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
import me.mikecasper.musicvoice.util.RecyclerViewItemClickListener;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private RecyclerViewItemClickListener mListener;
    private List<Playlist> mPlaylists;
    private Context mContext;

    public PlaylistAdapter(Context context, List<Playlist> playlists, RecyclerViewItemClickListener listener) {
        mPlaylists = playlists;
        mContext = context;
        mListener = listener;
    }

    public void setPlaylists(List<Playlist> playlists) {
        mPlaylists = playlists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View playlistView = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);

        return new ViewHolder(playlistView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Playlist playlist = mPlaylists.get(position);
        holder.mPlaylist = playlist;

        Picasso.with(mContext).load(playlist.getImages().get(0).getUrl()).placeholder(R.drawable.default_playlist).into(holder.mPlaylistArt);

        holder.mPlaylistTitle.setText(playlist.getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Playlist mPlaylist;
        public final View mView;
        public final ImageView mPlaylistArt;
        public final TextView mPlaylistTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mPlaylistArt = (ImageView) itemView.findViewById(R.id.playlistArt);
            mPlaylistTitle = (TextView) itemView.findViewById(R.id.playlistTitle);
        }
    }
}
