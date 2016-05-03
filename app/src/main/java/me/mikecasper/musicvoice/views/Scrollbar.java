package me.mikecasper.musicvoice.views;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import me.mikecasper.musicvoice.R;

public class Scrollbar extends LinearLayout {
    private int mHeight;
    private View mBar;
    private RecyclerView mRecylerView;

    public Scrollbar(Context context) {
        super(context);
    }

    public Scrollbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public Scrollbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        super.setOrientation(HORIZONTAL);
        super.setClipChildren(false);
        LayoutInflater.from(context).inflate(R.layout.scrollbar, this);
        mBar = findViewById(R.id.scrollbar);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.mRecylerView = recyclerView;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    moveBar((RecyclerView) v);
                }
            });
        } else {
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    moveBar(recyclerView);
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            setPosition(event.getY());
            setRecyclerViewPosition(event.getY());
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
    }

    private void setRecyclerViewPosition(float y) {
        if (mRecylerView != null) {
            int itemCount = mRecylerView.getAdapter().getItemCount();
            float proportion = y / (float) mHeight;
            int position = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            mRecylerView.scrollToPosition(position);
        }
    }

    private void setPosition(float y) {
        float position = y / mHeight;
        int barHeight = mBar.getHeight();
        mBar.setY(getValueInRange(0, mHeight - barHeight, (int) ((mHeight - barHeight) * position)));
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void moveBar(RecyclerView recyclerView) {
        View firstVisibleView = recyclerView.getChildAt(0);
        int firstVisiblePosition = recyclerView.getChildAdapterPosition(firstVisibleView);
        int visibleRange = recyclerView.getChildCount();
        int lastVisiblePosition = firstVisiblePosition + visibleRange;
        int itemCount = recyclerView.getAdapter().getItemCount();

        int position;
        if (firstVisiblePosition == 0) {
            position = 0;
        } else if (lastVisiblePosition == itemCount - 1) {
            position = itemCount - 1;
        } else {
            position = firstVisiblePosition;
        }

        float proportion = (float) position / (float) itemCount;
        setPosition(mHeight * proportion);
    }
}
