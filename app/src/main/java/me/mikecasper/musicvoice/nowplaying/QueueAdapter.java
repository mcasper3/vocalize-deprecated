package me.mikecasper.musicvoice.nowplaying;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.util.RecyclerViewItemClickListener;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private static final int TITLE_TYPE = 0;
    private static final int NOW_PLAYING_TYPE = 1;
    private static final int QUEUE_ITEM_TYPE = 2;

    private RecyclerViewItemClickListener mListener;
    private List<Track> mQueue;
    private List<Track> mPriorityQueue;
    private String mPlaylistName;

    public QueueAdapter(List<Track> queue, List<Track> priorityQueue, String playlistName, RecyclerViewItemClickListener listener) {
        mListener = listener;
        mQueue = queue;
        mPriorityQueue = priorityQueue;
        mPlaylistName = playlistName;
    }

    public void updateQueues(List<Track> queue, List<Track> priorityQueue) {
        mQueue = queue;
        mPriorityQueue = priorityQueue;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int type;

        boolean includePriorityQueue = mPriorityQueue != null && mPriorityQueue.size() > 0;

        if (position == 0) {
            type = TITLE_TYPE;
        } else if (position == 1) {
            type = NOW_PLAYING_TYPE;
        } else if (position == 2) {
            type = TITLE_TYPE;
        } else if (includePriorityQueue && position == mPriorityQueue.size() + 3) {
            type = TITLE_TYPE;
        } else {
            type = QUEUE_ITEM_TYPE;
        }

        return type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        View view;

        switch (viewType) {
            case TITLE_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_queue_title, parent, false);
                viewHolder = new TitleViewHolder(view);
                break;
            case NOW_PLAYING_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_now_playing, parent, false);
                viewHolder = new NowPlayingViewHolder(view);
                break;
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_queue, parent, false);
                viewHolder = new QueueItemViewHolder(view);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        boolean includePriorityQueue = mPriorityQueue != null && mPriorityQueue.size() > 0;

        if (position == 0) {
            Context context = holder.mView.getContext();

            String title = context.getString(R.string.now_playing);

            bindTitle(title, holder);
        } else if (position == 1) {
            bindNowPlaying(holder);
        } else if (position == 2) {
            Context context = holder.mView.getContext();
            String title;

            if (includePriorityQueue) {
                title = context.getString(R.string.now_playing);
            } else {
                title = context.getString(R.string.next_from, mPlaylistName);
            }

            bindTitle(title, holder);
        } else if (includePriorityQueue && position == mPriorityQueue.size() + 3) {
            Context context = holder.mView.getContext();
            String title = context.getString(R.string.next_from, mPlaylistName);

            bindTitle(title, holder);
        } else {
            if (includePriorityQueue) {
                if (position < mPriorityQueue.size() + 3) {
                    bindQueueItem(holder, true, position - 3);
                } else {
                    bindQueueItem(holder, false, position - 4 - mPriorityQueue.size());
                }
            } else {
                // position - 2 is the position in the queue since at position 3, we will want to get the second item in the queue
                bindQueueItem(holder, false, position - 3);
            }
        }
    }

    private void bindTitle(String title, ViewHolder viewHolder) {
        TitleViewHolder titleViewHolder = (TitleViewHolder) viewHolder;

        titleViewHolder.mTitle.setText(title);
    }

    private void bindQueueItem(ViewHolder viewHolder, boolean isPriorityQueue, int position) {
        final QueueItemViewHolder queueItemViewHolder = (QueueItemViewHolder) viewHolder;

        Track track;

        if (isPriorityQueue) {
            track = mPriorityQueue.get(position);
        } else {
            track = mQueue.get(position);
        }

        queueItemViewHolder.mTrack = track;

        queueItemViewHolder.mTrackName.setText(track.getName());

        String artistNames = "";

        for (Artist artist : track.getArtists()) {
            artistNames += artist.getName() + ", ";
        }

        artistNames = artistNames.substring(0, artistNames.length() - 2);

        queueItemViewHolder.mArtistName.setText(artistNames);

        queueItemViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(queueItemViewHolder);
            }
        });
    }

    private void bindNowPlaying(ViewHolder viewHolder) {
        final NowPlayingViewHolder nowPlayingViewHolder = (NowPlayingViewHolder) viewHolder;

        Track track = mQueue.get(0);
        nowPlayingViewHolder.mTrack = track;

        nowPlayingViewHolder.mTrackName.setText(track.getName());

        String artistNames = "";

        for (Artist artist : track.getArtists()) {
            artistNames += artist.getName() + ", ";
        }

        artistNames = artistNames.substring(0, artistNames.length() - 2);

        nowPlayingViewHolder.mArtistName.setText(artistNames);

        Context context = nowPlayingViewHolder.mAlbumArt.getContext();
        Picasso.with(context)
                .load(track.getAlbum().getImages().get(0).getUrl())
                .fit()
                .into(nowPlayingViewHolder.mAlbumArt);

        nowPlayingViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO do something with this to go back to other fragment (check instanceof)
                mListener.onItemClick(nowPlayingViewHolder);
            }
        });
    }

    @Override
    public int getItemCount() {
        int size = 2;

        if (mQueue.size() > 1) {
            size = mQueue.size() + 2;
        }

        if (mPriorityQueue != null && mPriorityQueue.size() > 0) {
            size += mPriorityQueue.size() + 1;
        }

        return size;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
        }
    }

    public class QueueItemViewHolder extends ViewHolder {

        public Track mTrack;
        public final View mView;
        public final TextView mTrackName;
        public final TextView mArtistName;
        public final CheckBox mCheckBox;

        public QueueItemViewHolder(View view) {
            super(view);

            mView = view;
            mTrackName = (TextView) view.findViewById(R.id.queue_track_name);
            mArtistName = (TextView) view.findViewById(R.id.queue_artist_name);
            mCheckBox = (CheckBox) view.findViewById(R.id.queue_check_box);
        }
    }

    public class NowPlayingViewHolder extends ViewHolder {

        public Track mTrack;
        public final View mView;
        public final ImageView mAlbumArt;
        public final TextView mArtistName;
        public final TextView mTrackName;

        public NowPlayingViewHolder(View view) {
            super(view);

            mView = view;
            mAlbumArt = (ImageView) view.findViewById(R.id.now_playing_album_art);
            mArtistName = (TextView) view.findViewById(R.id.now_playing_artist_name);
            mTrackName = (TextView) view.findViewById(R.id.now_playing_track_name);
        }
    }

    public class TitleViewHolder extends ViewHolder {

        public final View mView;
        public final TextView mTitle;

        public TitleViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.section_title);
        }
    }
}
