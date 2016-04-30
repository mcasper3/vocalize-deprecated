package me.mikecasper.musicvoice.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mSpacing;

    public VerticalSpaceItemDecoration(float verticalSpaceHeight) {
        mSpacing = (int) Math.ceil(verticalSpaceHeight);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) % 2 == 0) {
            outRect.right = mSpacing;
        } else {
            outRect.left = mSpacing;
        }

        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = mSpacing;
        }
    }
}
