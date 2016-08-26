package me.mikecasper.vocalize.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import me.mikecasper.vocalize.R;

public class SquareImageView extends ImageView {

    private boolean mUseWidth;

    public SquareImageView(Context context) {
        super(context);

        mUseWidth = true;
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SquareImageView);

        try {
            mUseWidth = typedArray.getBoolean(R.styleable.SquareImageView_useWidth, true);
        } finally {
            typedArray.recycle();
        }
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SquareImageView);

        try {
            mUseWidth = typedArray.getBoolean(R.styleable.SquareImageView_useWidth, true);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mUseWidth) {
            int width = getMeasuredWidth();
            setMeasuredDimension(width, width);
        } else {
            int height = getMeasuredHeight();
            setMeasuredDimension(height, height);
        }
    }
}
