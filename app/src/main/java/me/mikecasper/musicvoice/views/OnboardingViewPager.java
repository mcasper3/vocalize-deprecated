package me.mikecasper.musicvoice.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import me.mikecasper.musicvoice.R;

public class OnboardingViewPager extends ViewPager {
    private boolean mSwipeable;

    public OnboardingViewPager(Context context) {
        super(context);

        mSwipeable = true;
    }

    public OnboardingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OnboardingViewPager);

        try {
            mSwipeable = typedArray.getBoolean(R.styleable.OnboardingViewPager_swipeable, true);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSwipeable && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeable && super.onTouchEvent(ev);
    }
}
