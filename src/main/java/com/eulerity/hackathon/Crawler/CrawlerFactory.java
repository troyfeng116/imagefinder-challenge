package com.eulerity.hackathon.Crawler;

import com.eulerity.hackathon.Crawler.Crawlers.ParallelBFSCrawler;
import com.eulerity.hackathon.Crawler.Crawlers.SerialBFSCrawler;

public class CrawlerFactory {
    private CrawlerFactory() {
    }

    public static Crawler create(CrawlerConfig aConfig) {
        return aConfig.getIsSingleThreaded() ? new SerialBFSCrawler(aConfig) : new ParallelBFSCrawler(aConfig);
    }
}
