package me.mikecasper.musicvoice.nowplaying;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.events.CheckBoxSelectedEvent;
import me.mikecasper.musicvoice.nowplaying.models.QueueItemInformation;
import me.mikecasper.musicvoice.util.RecyclerViewItemClickedEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private static final int TITLE_TYPE = 0;
    private static final int NOW_PLAYING_TYPE = 1;
    private static final int QUEUE_ITEM_TYPE = 2;

    private List<Track> mQueue;
    private List<Track> mPriorityQueue;
    private Track mNowPlaying;
    private String mPlaylistName;

    public QueueAdapter(Track nowPlaying, List<Track> queue, List<Track> priorityQueue, String playlistName) {
        mQueue = queue;
        mPriorityQueue = priorityQueue;
        mPlaylistName = playlistName;
        mNowPlaying = nowPlaying;
    }

    public void updateQueues(List<Track> queue, List<Track> priorityQueue, Track nowPlaying) {
        mQueue = queue;
        mPriorityQueue = priorityQueue;
        mNowPlaying = nowPlaying;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int type;

        boolean includePriorityQueue = mPriorityQueue.size() > 0;

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
        boolean includePriorityQueue = mPriorityQueue.size() > 0;

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
                title = context.getString(R.string.queued);
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

    private void bindQueueItem(ViewHolder viewHolder, boolean isPriorityQueue, int queuePosition) {
        final QueueItemViewHolder queueItemViewHolder = (QueueItemViewHolder) viewHolder;

        Track track;

        if (isPriorityQueue) {
            track = mPriorityQueue.get(queuePosition);
        } else {
            track = mQueue.get(queuePosition);
        }

        final QueueItemInformation info = new QueueItemInformation(queuePosition, isPriorityQueue);

        queueItemViewHolder.mTrackIndex = queuePosition;
        queueItemViewHolder.mIsPriorityQueue = isPriorityQueue;

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
                Context context = v.getContext();

                IEventManager eventManager = EventManagerProvider.getInstance(context);
                eventManager.postEvent(new RecyclerViewItemClickedEvent(queueItemViewHolder));
            }
        });

        queueItemViewHolder.mCheckBox.setChecked(false);
        queueItemViewHolder.mCheckBox.setClickable(false);

        queueItemViewHolder.mCheckBoxWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !queueItemViewHolder.mCheckBox.isChecked();
                queueItemViewHolder.mCheckBox.setChecked(isChecked);

                Context context = v.getContext();

                IEventManager eventManager = EventManagerProvider.getInstance(context);
                eventManager.postEvent(new CheckBoxSelectedEvent(info, isChecked));
            }
        });
    }

    private void bindNowPlaying(ViewHolder viewHolder) {
        final NowPlayingViewHolder nowPlayingViewHolder = (NowPlayingViewHolder) viewHolder;

        nowPlayingViewHolder.mTrackName.setText(mNowPlaying.getName());

        String artistNames = "";

        for (Artist artist : mNowPlaying.getArtists()) {
            artistNames += artist.getName() + ", ";
        }

        artistNames = artistNames.substring(0, artistNames.length() - 2);

        nowPlayingViewHolder.mArtistName.setText(artistNames);

        Context context = nowPlayingViewHolder.mAlbumArt.getContext();
        Picasso.with(context)
                .load(mNowPlaying.getAlbum().getImages().get(0).getUrl())
                .fit()
                .into(nowPlayingViewHolder.mAlbumArt);

        nowPlayingViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();

                IEventManager eventManager = EventManagerProvider.getInstance(context);
                eventManager.postEvent(new RecyclerViewItemClickedEvent(nowPlayingViewHolder));
            }
        });
    }

    @Override
    public int getItemCount() {
        int size = 0;

        if (mNowPlaying != null) {
            size += 2;
        }

        int queueSize = mQueue.size();
        if (queueSize > 0) {
            size += queueSize + 1;
        }

        int priorityQueueSize = mPriorityQueue.size();
        if (priorityQueueSize > 0) {
            size += priorityQueueSize + 1;
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

        public int mTrackIndex;
        public boolean mIsPriorityQueue;
        public final View mView;
        public final TextView mTrackName;
        public final TextView mArtistName;
        public final CheckBox mCheckBox;
        public final View mCheckBoxWrapper;

        public QueueItemViewHolder(View view) {
            super(view);

            mView = view;
            mTrackName = (TextView) view.findViewById(R.id.queue_track_name);
            mArtistName = (TextView) view.findViewById(R.id.queue_artist_name);
            mCheckBox = (CheckBox) view.findViewById(R.id.queue_check_box);
            mCheckBoxWrapper = view.findViewById(R.id.queue_check_box_wrapper);
        }
    }

    public class NowPlayingViewHolder extends ViewHolder {

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
