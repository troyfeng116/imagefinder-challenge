package com.eulerity.hackathon.Crawler;

import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

import crawlercommons.robots.SimpleRobotRules;

public abstract class Crawler {
    private final CrawlerConfig theCrawlerConfig;
    private final SimpleRobotRules theRobotRules;
    private final RetryPolicy theRetryPolicy;

    public Crawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules, RetryPolicy aRetryPolicy) {
        theCrawlerConfig = aCrawlerConfig;
        theRobotRules = aRobotRules;
        theRetryPolicy = aRetryPolicy;
    }

    public final CrawlerConfig getCrawlerConfig() {
        return theCrawlerConfig;
    }

    public final SimpleRobotRules getRobotRules() {
        return theRobotRules;
    }

    public final RetryPolicy getRetryPolicy() {
        return theRetryPolicy;
    }

    public abstract CrawlerResults crawlAndScrape();
}
