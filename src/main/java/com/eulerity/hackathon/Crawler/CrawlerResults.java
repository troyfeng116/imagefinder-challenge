package com.eulerity.hackathon.Crawler;

import java.util.List;

public class CrawlerResults {
    private final CrawlerConfig theConfig;
    private final List<String> theImgSrcs;
    private final List<String> theCrawledUrls;
    private final long theCrawlTimeMs;

    private CrawlerResults(Builder aBuilder) {
        theConfig = aBuilder.theConfig;
        theImgSrcs = aBuilder.theImgSrcs;
        theCrawledUrls = aBuilder.theCrawledUrls;
        theCrawlTimeMs = aBuilder.theCrawlTimeMs;
    }

    public CrawlerConfig getConfig() {
        return theConfig;
    }

    public List<String> getImgSrcs() {
        return theImgSrcs;
    }

    public List<String> getCrawledUrls() {
        return theCrawledUrls;
    }

    public long getCrawlTimeMs() {
        return theCrawlTimeMs;
    }

    public static class Builder {
        private final CrawlerConfig theConfig;
        private List<String> theImgSrcs;
        private List<String> theCrawledUrls;
        private long theCrawlTimeMs;

        public Builder(CrawlerConfig aConfig) {
            theConfig = aConfig;
        }

        public Builder withImgSrcs(List<String> aImgSrcs) {
            theImgSrcs = aImgSrcs;
            return this;
        }

        public Builder withCrawledUrls(List<String> aCrawledUrls) {
            theCrawledUrls = aCrawledUrls;
            return this;
        }

        public Builder withCrawlTimeMs(long aCrawlTimeMs) {
            theCrawlTimeMs = aCrawlTimeMs;
            return this;
        }

        public CrawlerResults build() {
            return new CrawlerResults(this);
        }
    }
}
