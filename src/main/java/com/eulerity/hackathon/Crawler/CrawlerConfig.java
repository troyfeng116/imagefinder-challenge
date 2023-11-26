package com.eulerity.hackathon.Crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import com.eulerity.hackathon.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CrawlerConfig {
    private final URL theStartUrl;
    private final int theMaxDepth;
    private final int theMaxUrls;
    private final int theMaxImgSrcs;
    private final boolean theShouldIncludeSVGs;
    private final boolean theShouldIncludePNGs;
    private final boolean theIsSingleThreaded;
    private final long theMaxCrawlTimeMs;

    private CrawlerConfig(Builder aBuilder) {
        theStartUrl = aBuilder.theStartUrl;
        theMaxDepth = aBuilder.theMaxDepth;
        theMaxUrls = aBuilder.theMaxUrls;
        theMaxImgSrcs = aBuilder.theMaxImgSrcs;
        theShouldIncludeSVGs = aBuilder.theShouldIncludeSVGs;
        theShouldIncludePNGs = aBuilder.theShouldIncludePNGs;
        theIsSingleThreaded = aBuilder.theIsSingleThreaded;
        theMaxCrawlTimeMs = aBuilder.theMaxCrawlTimeMs;
    }

    /**
     * Constructs `CrawlerConfig` object from req JSON, with default values if
     * absent from req JSON. `url` field required.
     *
     * @param aReqJson JSON from incoming req.
     * @return `CrawlerConfig` object, with default values for missing fields.
     * 
     * @throws IllegalArgumentException If `url` field does not exist in req JSON.
     * @throws MalformedURLException    If `url` field is malformed.
     * 
     * @see `com.eulerity.hackathon.Constants` for field defaults.
     */
    public static CrawlerConfig of(JsonObject aReqJson) throws IllegalArgumentException, MalformedURLException {
        JsonElement myUrlElement = aReqJson.get(Constants.URL_FIELD);
        if (myUrlElement == null) {
            throw new IllegalArgumentException(
                    String.format("[%s] req json must contain `%s` field", Constants.URL_FIELD, CrawlerConfig.class));
        }

        String myUrlString = myUrlElement.getAsString();
        URL myUrl = new URL(myUrlString);

        return new Builder(myUrl)
                .withMaxDepth(readFieldFromJson(aReqJson,
                        Constants.MAX_DEPTH_FIELD, Constants.DEFAULT_MAX_DEPTH,
                        aJsonElement -> aJsonElement.getAsInt()))
                .withMaxUrls(readFieldFromJson(aReqJson,
                        Constants.MAX_URLS_FIELD, Constants.DEFAULT_MAX_URLS,
                        aJsonElement -> aJsonElement.getAsInt()))
                .withMaxImgSrcs(readFieldFromJson(aReqJson,
                        Constants.MAX_IMG_SRCS_FIELD, Constants.DEFAULT_MAX_IMG_SRCS,
                        aJsonElement -> aJsonElement.getAsInt()))
                .withShouldIncludeSVGs(readFieldFromJson(aReqJson,
                        Constants.SHOULD_INCLUDE_SVGS_FIELD, Constants.DEFAULT_SHOULD_INCLUDE_SVGS,
                        aJsonElement -> aJsonElement.getAsBoolean()))
                .withShouldIncludePNGs(readFieldFromJson(aReqJson,
                        Constants.SHOULD_INCLUDE_PNGS_FIELD, Constants.DEFAULT_SHOULD_INCLUDE_PNGS,
                        aJsonElement -> aJsonElement.getAsBoolean()))
                .withIsSingleThreaded(readFieldFromJson(aReqJson,
                        Constants.IS_SINGLE_THREADED_FIELD, Constants.DEFAULT_IS_SINGLE_THREADED,
                        aJsonElement -> aJsonElement.getAsBoolean()))
                .withMaxCrawlTimeMs(readFieldFromJson(aReqJson,
                        Constants.MAX_CRAWL_TIME_MS_FIELD, Constants.DEFAULT_MAX_CRAWL_TIME_MS,
                        aJsonElement -> aJsonElement.getAsLong()))
                .build();
    }

    public URL getStartUrl() {
        return theStartUrl;
    }

    public int getMaxDepth() {
        return theMaxDepth;
    }

    public int getMaxUrls() {
        return theMaxUrls;
    }

    public int getMaxImgSrcs() {
        return theMaxImgSrcs;
    }

    public boolean getShouldIncludeSVGs() {
        return theShouldIncludeSVGs;
    }

    public boolean getShouldIncludePNGs() {
        return theShouldIncludePNGs;
    }

    public boolean getIsSingleThreaded() {
        return theIsSingleThreaded;
    }

    public long getMaxCrawlTimeMs() {
        return theMaxCrawlTimeMs;
    }

    @Override
    public String toString() {
        return String.format(
                "CrawlerConfig{%s, maxDepth=%d, maxUrls=%d, maxImgSrcs=%d, shouldIncludeSVGs=%b, shouldIncludePNGs=%b, isSingleThreaded=%b, maxCrawlTimeMs=%d}",
                theStartUrl.toString(),
                theMaxDepth, theMaxUrls, theMaxImgSrcs,
                theShouldIncludeSVGs, theShouldIncludePNGs, theIsSingleThreaded,
                theMaxCrawlTimeMs);
    }

    private static <T> T readFieldFromJson(JsonObject aReqJson, String aFieldName, T aDefaultValue,
            JsonElementGetter<T> aJsonElementGetter) {
        Optional<JsonElement> myOptionalJson = Optional.ofNullable(aReqJson.get(aFieldName));
        return myOptionalJson.isPresent() ? aJsonElementGetter.getFromJsonElement(myOptionalJson.get()) : aDefaultValue;
    }

    public static class Builder {
        private final URL theStartUrl;
        private int theMaxDepth;
        private int theMaxUrls;
        private int theMaxImgSrcs;
        private boolean theShouldIncludeSVGs;
        private boolean theShouldIncludePNGs;
        private boolean theIsSingleThreaded;
        private long theMaxCrawlTimeMs;

        public Builder(URL aStartUrl) {
            theStartUrl = aStartUrl;
        }

        public Builder withMaxDepth(int aMaxDepth) {
            theMaxDepth = aMaxDepth;
            return this;
        }

        public Builder withMaxUrls(int aMaxUrls) {
            theMaxUrls = aMaxUrls;
            return this;
        }

        public Builder withMaxImgSrcs(int aMaxImgSrcs) {
            theMaxImgSrcs = aMaxImgSrcs;
            return this;
        }

        public Builder withShouldIncludeSVGs(boolean aShouldIncludeSVGs) {
            theShouldIncludeSVGs = aShouldIncludeSVGs;
            return this;
        }

        public Builder withShouldIncludePNGs(boolean aShouldIncludePNGs) {
            theShouldIncludePNGs = aShouldIncludePNGs;
            return this;
        }

        public Builder withIsSingleThreaded(boolean aIsSingleThreaded) {
            theIsSingleThreaded = aIsSingleThreaded;
            return this;
        }

        public Builder withMaxCrawlTimeMs(long aMaxCrawlTimeMs) {
            theMaxCrawlTimeMs = aMaxCrawlTimeMs;
            return this;
        }

        public CrawlerConfig build() {
            return new CrawlerConfig(this);
        }
    }

    @FunctionalInterface
    private static interface JsonElementGetter<T> {
        T getFromJsonElement(JsonElement aJsonElement);
    }
}
