package com.eulerity.hackathon.Crawler;

import crawlercommons.robots.SimpleRobotRules;

public abstract class Crawler {
    private final CrawlerConfig theCrawlerConfig;
    private final SimpleRobotRules theRobotRules;

    public Crawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules) {
        theCrawlerConfig = aCrawlerConfig;
        theRobotRules = aRobotRules;
    }

    public final CrawlerConfig getCrawlerConfig() {
        return theCrawlerConfig;
    }

    public final SimpleRobotRules getRobotRules() {
        return theRobotRules;
    }

    public abstract CrawlerResults crawlAndScrape();
}
