package me.mikecasper.musicvoice.util;

public final class DateUtility {

    private static final int HOUR_IN_MS = 3600000;
    private static final int MINUTE_IN_MS = 60000;

    private DateUtility() { }

    public static String formatDuration(int duration) {
        int hours = duration / HOUR_IN_MS;
        int minutes = (duration % HOUR_IN_MS) / MINUTE_IN_MS;
        int seconds = (duration % HOUR_IN_MS % MINUTE_IN_MS) / 1000;

        String result = "";

        if (hours > 0) {
            result += hours + ":";
        }

        result += minutes + ":" + seconds;

        return result;
    }
}
