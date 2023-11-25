package com.eulerity.hackathon.Crawler;

import com.eulerity.hackathon.Crawler.Crawlers.ParallelBFSCrawler;
import com.eulerity.hackathon.Crawler.Crawlers.SerialBFSCrawler;
import com.eulerity.hackathon.Scraper.RetryPolicy.ExponentialDropoffRetryPolicy;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

import crawlercommons.robots.SimpleRobotRules;

public class CrawlerFactory {
    private CrawlerFactory() {
    }

    public static Crawler create(CrawlerConfig aConfig, SimpleRobotRules aRobotRules) {
        long myRobotsCrawlDelayMs = aRobotRules.getCrawlDelay() * 1000;
        System.out.printf("[CrawlerFactory] robots delay = %d ms\n", myRobotsCrawlDelayMs);
        RetryPolicy mRetryPolicy = new ExponentialDropoffRetryPolicy(3, Math.max(myRobotsCrawlDelayMs, 1000), 1.9);

        // use serial crawler to avoid blasting sites with robots.txt crawl delay
        boolean myShouldUseSerial = aConfig.getIsSingleThreaded() || myRobotsCrawlDelayMs != 0;
        System.out.printf("[CrawlerFactory] using serial crawler: %b\n", myShouldUseSerial);

        return myShouldUseSerial ? new SerialBFSCrawler(aConfig, aRobotRules, mRetryPolicy)
                : new ParallelBFSCrawler(aConfig, aRobotRules, mRetryPolicy);
    }
}
