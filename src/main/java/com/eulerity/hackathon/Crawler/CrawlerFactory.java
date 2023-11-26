package com.eulerity.hackathon.Crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eulerity.hackathon.Crawler.Crawlers.ParallelBFSCrawler;
import com.eulerity.hackathon.Crawler.Crawlers.SerialBFSCrawler;
import com.eulerity.hackathon.Scraper.RetryPolicy.ExponentialDropoffRetryPolicy;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

import crawlercommons.robots.SimpleRobotRules;

public class CrawlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerFactory.class);

    private CrawlerFactory() {
    }

    public static Crawler create(CrawlerConfig aConfig, SimpleRobotRules aRobotRules) {
        long myRobotsCrawlDelayMs = aRobotRules.getCrawlDelay() * 1000;
        LOGGER.debug(String.format("robots delay = %d ms", myRobotsCrawlDelayMs));
        RetryPolicy mRetryPolicy = new ExponentialDropoffRetryPolicy(3, Math.max(myRobotsCrawlDelayMs, 1000), 1.9);

        // use serial crawler to avoid blasting sites with robots.txt crawl delay
        boolean myShouldUseSerial = aConfig.getIsSingleThreaded() || myRobotsCrawlDelayMs != 0;
        LOGGER.debug(String.format("using serial crawler: %b", myShouldUseSerial));

        return myShouldUseSerial ? new SerialBFSCrawler(aConfig, aRobotRules, mRetryPolicy)
                : new ParallelBFSCrawler(aConfig, aRobotRules, mRetryPolicy);
    }
}
