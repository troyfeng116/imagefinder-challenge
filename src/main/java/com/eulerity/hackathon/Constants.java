package com.eulerity.hackathon;

public class Constants {
    private Constants() {
    }

    public static final String URL_FIELD = "url";

    public static final String MAX_DEPTH_FIELD = "maxDepth";
    public static final int DEFAULT_MAX_DEPTH = 5;

    public static final String MAX_URLS_FIELD = "maxUrls";
    public static final int DEFAULT_MAX_URLS = 100;

    public static final String MAX_IMG_SRCS_FIELD = "maxImgs";
    public static final int DEFAULT_MAX_IMG_SRCS = 200;

    public static final String SHOULD_INCLUDE_SVGS_FIELD = "shouldIncludeSvgs";
    public static final boolean DEFAULT_SHOULD_INCLUDE_SVGS = false;

    public static final String SHOULD_INCLUDE_PNGS_FIELD = "shouldIncludePngs";
    public static final boolean DEFAULT_SHOULD_INCLUDE_PNGS = false;

    public static final String IS_SINGLE_THREADED_FIELD = "isSingleThreaded";
    public static final boolean DEFAULT_IS_SINGLE_THREADED = false;

    public static final String MAX_CRAWL_TIME_MS_FIELD = "maxCrawlTimeMs";
    public static final long DEFAULT_MAX_CRAWL_TIME_MS = 5000;

    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";
}
