package me.mikecasper.musicvoice.caching.track;

import android.provider.BaseColumns;

public final class TrackContract {

    public static abstract class TrackEntry implements BaseColumns {
        public static final String TABLE_NAME = "track";
        public static final String URI = "uri";
        public static final String TOTAL = "total";
        public static final String NAME = "name";
        public static final String DURATION = "duration";
        public static final String IS_PLAYABLE = "is_playable";
    }
}
