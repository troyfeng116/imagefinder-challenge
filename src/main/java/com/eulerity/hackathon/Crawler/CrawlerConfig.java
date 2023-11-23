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

    private CrawlerConfig(Builder aBuilder) {
        theStartUrl = aBuilder.theStartUrl;
        theMaxDepth = aBuilder.theMaxDepth;
        theMaxUrls = aBuilder.theMaxUrls;
        theMaxImgSrcs = aBuilder.theMaxImgSrcs;
    }

    // TODO: throw exceptions
    public static CrawlerConfig of(JsonObject aReqJson) {
        JsonElement myUrlElement = aReqJson.get(Constants.URL_FIELD);
        if (myUrlElement == null) {
            System.err.println(new IllegalArgumentException(
                    String.format("[%s] req json must contain `%s` field", Constants.URL_FIELD, CrawlerConfig.class)));
            return null;
        }

        String myUrlString = myUrlElement.getAsString();
        URL myUrl;
        try {
            myUrl = new URL(myUrlString);
        } catch (MalformedURLException myException) {
            System.err.println(myException);
            return null;
        }

        return new Builder()
                .withStartUrl(myUrl)
                .withMaxDepth(readIntField(aReqJson, Constants.MAX_DEPTH_FIELD, Constants.DEFAULT_MAX_DEPTH))
                .withMaxUrls(readIntField(aReqJson, Constants.MAX_URLS_FIELD, Constants.DEFAULT_MAX_URLS))
                .withMaxImgSrcs(readIntField(aReqJson, Constants.MAX_IMG_SRCS_FIELD, Constants.DEFAULT_MAX_IMG_SRCS))
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

    @Override
    public String toString() {
        return String.format("CrawlerConfig{%s, maxDepth=%d, maxUrls=%d, maxImgSrcs=%d}\n", theStartUrl.toString(),
                theMaxDepth, theMaxUrls, theMaxImgSrcs);
    }

    private static int readIntField(JsonObject aReqJson, String aFieldName, int aDefaultValue) {
        Optional<JsonElement> myOptionalJson = Optional.ofNullable(aReqJson.get(aFieldName));
        return myOptionalJson.isPresent() ? myOptionalJson.get().getAsInt() : aDefaultValue;
    }

    public static class Builder {
        private URL theStartUrl;
        private Integer theMaxDepth;
        private Integer theMaxUrls;
        private Integer theMaxImgSrcs;

        public Builder() {
            theStartUrl = null;
            theMaxDepth = null;
            theMaxUrls = null;
            theMaxImgSrcs = null;
        }

        public Builder withStartUrl(URL aStartUrl) {
            theStartUrl = aStartUrl;
            return this;
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

        public CrawlerConfig build() {
            return new CrawlerConfig(this);
        }
    }
}