package me.mikecasper.vocalize.helpers;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class DrawableMatcher extends TypeSafeMatcher<View> {

    private int mResourceId;
    private String resourceName;

    public DrawableMatcher(int resourceId) {
        super(View.class);
        mResourceId = resourceId;
    }

    @Override
    protected boolean matchesSafely(View item) {
        if (!(item instanceof ImageView)) {
            return false;
        }

        ImageView imageView = (ImageView) item;
        if (mResourceId < 0) {
            // If a resource id of -1 is passed in, there should be no drawable in the ImageView
            return imageView.getDrawable() == null;
        }

        resourceName = item.getResources().getResourceName(mResourceId);
        Drawable expected = ContextCompat.getDrawable(item.getContext(), mResourceId);
        if (expected == null) {
            return false;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        BitmapDrawable expectedBitmapDrawable = (BitmapDrawable) expected;
        Bitmap expectedBitmap = expectedBitmapDrawable.getBitmap();

        return expectedBitmap.sameAs(bitmap);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with drawable from resource id: ");
        description.appendValue(mResourceId);
        if (resourceName != null) {
            description.appendText("[");
            description.appendText(resourceName);
            description.appendText("]");
        }
    }
}
