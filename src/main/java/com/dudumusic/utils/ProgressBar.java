package com.dudumusic.utils;

public class ProgressBar {

    public static String create(long current, long total, int length) {
        if (total == 0 || total == Long.MAX_VALUE) {
            return "[" + "─".repeat(length) + "] LIVE";
        }

        int progress = (int) ((double) current / total * length);
        progress = Math.min(progress, length);

        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < length; i++) {
            if (i < progress) {
                bar.append("█");
            } else {
                bar.append("─");
            }
        }

        bar.append("] ");

        int percentage = (int) ((double) current / total * 100);
        bar.append(percentage).append("%");

        return bar.toString();
    }


    public static String createWithTime(long current, long total, int length) {
        String bar = create(current, total, length);
        String currentTime = TimeFormat.format(current);
        String totalTime = TimeFormat.format(total);

        return String.format("%s\n%s / %s", bar, currentTime, totalTime);
    }
}
