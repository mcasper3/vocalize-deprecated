package me.mikecasper.musicvoice.caching;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.concurrent.Semaphore;

import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.caching.track.TrackTableManager;

public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    private static DatabaseManager sInstance;

    // Table Managers
    private TrackTableManager mTrackTableManager;

    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;
    private Semaphore mSemaphore;
    private Context mContext;

    private DatabaseManager(Context context) {
        mSemaphore = new Semaphore(1, true);
        mContext = context;
    }

    public static DatabaseManager getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseManager(context);
        }

        return sInstance;
    }

    public void open() {
        try {
            mSemaphore.acquire();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted when acquiring semaphore", e);
        }
    }
}
