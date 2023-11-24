package com.eulerity.hackathon.Crawler;

import com.eulerity.hackathon.Crawler.Crawlers.ParallelBFSCrawler;
import com.eulerity.hackathon.Crawler.Crawlers.SerialBFSCrawler;

import crawlercommons.robots.SimpleRobotRules;

public class CrawlerFactory {
    private CrawlerFactory() {
    }

    public static Crawler create(CrawlerConfig aConfig, SimpleRobotRules aRobotRules) {
        return aConfig.getIsSingleThreaded() ? new SerialBFSCrawler(aConfig, aRobotRules)
                : new ParallelBFSCrawler(aConfig, aRobotRules);
    }
}
