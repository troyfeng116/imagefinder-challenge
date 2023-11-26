package com.eulerity.hackathon.Crawler;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

import crawlercommons.robots.SimpleRobotRules;

public abstract class Crawler {
    private final CrawlerConfig theCrawlerConfig;
    private final SimpleRobotRules theRobotRules;
    private final RetryPolicy theRetryPolicy;
    private final CrawlerNotifier theCrawlerNotifier;

    public Crawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules, RetryPolicy aRetryPolicy,
            CrawlerNotifier aCrawlerNotifier) {
        theCrawlerConfig = aCrawlerConfig;
        theRobotRules = aRobotRules;
        theRetryPolicy = aRetryPolicy;
        theCrawlerNotifier = aCrawlerNotifier;
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

    public final CrawlerNotifier getCrawlerNotifier() {
        return theCrawlerNotifier;
    }

    public CrawlerResults crawlAndScrape() {
        long myStartTimestampMs = System.currentTimeMillis();

        CrawlerConfig myCrawlerConfig = getCrawlerConfig();
        CrawlerNotifier myNotifier = getCrawlerNotifier();

        URL myStartUrl = myCrawlerConfig.getStartUrl();
        String myStartUrlString = myStartUrl.toString();
        myNotifier.checkAndNotifyHref(myStartUrlString);

        Queue<String> myLevelUrls = new LinkedList<>();
        for (int myLevel = 0; myLevel < myCrawlerConfig.getMaxDepth()
                && myNotifier.drainNextUrlsQueue(myLevelUrls) > 0; myLevel++) {
            System.out.printf("[%s] scraping level=%d, %d new urls, %d seen urls\n", getClass(), myLevel,
                    myLevelUrls.size(), myNotifier.getAllSeenUrls().size());
            handleCrawlLevel(myStartTimestampMs, myLevel, myLevelUrls);
        }

        long myElapsedTimeMs = CrawlerUtils.getTimeElapsedSinceMs(myStartTimestampMs);
        System.out.printf(
                "[%s] returning %d discovered img srcs after pushing %d urls onto BFS queue in %d ms\n",
                getClass(),
                myNotifier.getDiscoveredImgSrcs().size(),
                myNotifier.getAllSeenUrls().size(),
                myElapsedTimeMs);
        return new CrawlerResults.Builder(myCrawlerConfig)
                .withImgSrcs(myNotifier.getDiscoveredImgSrcs())
                .withCrawledUrls(myNotifier.getAllSeenUrls())
                .withCrawlTimeMs(myElapsedTimeMs)
                .build();
    }

    public abstract boolean handleCrawlLevel(long aStartTimestampMs, int aLevel, Queue<String> aLevelUrls);
}
