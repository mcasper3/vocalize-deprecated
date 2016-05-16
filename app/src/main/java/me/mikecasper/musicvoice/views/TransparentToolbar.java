package me.mikecasper.musicvoice.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TransparentToolbar extends Toolbar {
    public TransparentToolbar(Context context) {
        super(context);
    }

    public TransparentToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TransparentToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
