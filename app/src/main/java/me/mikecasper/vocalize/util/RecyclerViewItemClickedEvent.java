package me.mikecasper.vocalize.util;

import android.support.v7.widget.RecyclerView;

public class RecyclerViewItemClickedEvent {

    private RecyclerView.ViewHolder mViewHolder;

    public RecyclerViewItemClickedEvent(RecyclerView.ViewHolder viewHolder) {
        mViewHolder = viewHolder;
    }

    public RecyclerView.ViewHolder getViewHolder() {
        return mViewHolder;
    }
}
