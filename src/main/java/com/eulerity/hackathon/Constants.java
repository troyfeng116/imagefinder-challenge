package com.eulerity.hackathon;

public enum Constants {
    INSTANCE;

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
}
