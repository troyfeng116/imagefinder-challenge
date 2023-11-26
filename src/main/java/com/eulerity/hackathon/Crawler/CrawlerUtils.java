package com.eulerity.hackathon.Crawler;

public class CrawlerUtils {
    private CrawlerUtils() {
    }

    public static long getTimeElapsedSinceMs(long aStartTimestampMs) {
        return System.currentTimeMillis() - aStartTimestampMs;
    }
}
