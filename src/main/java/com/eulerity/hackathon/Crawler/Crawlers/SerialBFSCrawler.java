package com.eulerity.hackathon.Crawler.Crawlers;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import com.eulerity.hackathon.Crawler.Crawler;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerResults;
import com.eulerity.hackathon.Crawler.CrawlerUtils;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Crawler.Notifiers.SerialCrawlerNotifier;
import com.eulerity.hackathon.Scraper.Scraper;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

import crawlercommons.robots.SimpleRobotRules;

public class SerialBFSCrawler extends Crawler {
    public SerialBFSCrawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules, RetryPolicy aRetryPolicy) {
        super(aCrawlerConfig, aRobotRules, aRetryPolicy);
    }

    @Override
    public CrawlerResults crawlAndScrape() {
        long myStartTimestampMs = System.currentTimeMillis();

        CrawlerConfig myCrawlerConfig = getCrawlerConfig();
        CrawlerNotifier myNotifier = new SerialCrawlerNotifier(myCrawlerConfig.getMaxImgSrcs(),
                myCrawlerConfig.getMaxUrls(), getRobotRules());
        long myRobotsCrawlDelayMs = getRobotRules().getCrawlDelay() * 1000;

        URL myStartUrl = myCrawlerConfig.getStartUrl();
        String myStartUrlString = myStartUrl.toString();
        myNotifier.checkAndNotifyHref(myStartUrlString);

        Queue<String> myLevelUrls = new LinkedList<>();
        boolean myHasTimeRemaining = true;
        for (int myLevel = 0; myHasTimeRemaining && myLevel < myCrawlerConfig.getMaxDepth()
                && myNotifier.drainNextUrlsQueue(myLevelUrls) > 0; myLevel++) {
            System.out.printf("[SerialBFSCrawler] scraping level=%d, %d new urls, %d seen urls\n", myLevel,
                    myLevelUrls.size(), myNotifier.getAllSeenUrls().size());

            long myRemainingCrawlTimeMs;
            while (!myLevelUrls.isEmpty()
                    && myNotifier.getDiscoveredImgSrcs().size() < myCrawlerConfig.getMaxImgSrcs()
                    && (myRemainingCrawlTimeMs = myCrawlerConfig.getMaxCrawlTimeMs()
                            - CrawlerUtils.getTimeElapsedSinceMs(myStartTimestampMs)) > 0) {
                long myUrlStartTimestampMs = System.currentTimeMillis();
                String myUrlToScrape = myLevelUrls.poll();
                if (Scraper.scrape(myUrlToScrape, myCrawlerConfig, myNotifier, getRetryPolicy(),
                        myRemainingCrawlTimeMs)) {
                    // serial crawler used for sites with robots.txt crawl-delay specified
                    // -> sleep until delay elapses if needed
                    long myUrlElapsedTimeMs = CrawlerUtils.getTimeElapsedSinceMs(myUrlStartTimestampMs);
                    long myUntilCanCrawlAgainMs = myRobotsCrawlDelayMs - myUrlElapsedTimeMs;
                    if (myUntilCanCrawlAgainMs > 0) {
                        myRemainingCrawlTimeMs = myCrawlerConfig.getMaxCrawlTimeMs()
                                - CrawlerUtils.getTimeElapsedSinceMs(myStartTimestampMs);
                        // don't bother scraping again if remaining crawl time not long enough for
                        // robots.txt crawl delay
                        if (myUntilCanCrawlAgainMs >= myRemainingCrawlTimeMs) {
                            myHasTimeRemaining = false;
                            break;
                        }

                        System.out.printf(
                                "[SerialBFSCrawler] scraped url %s: sleeping for %d ms after scrape time %d ms to respect robots.txt crawl delay %d ms\n",
                                myUrlToScrape, myUntilCanCrawlAgainMs, myUrlElapsedTimeMs, myRobotsCrawlDelayMs);
                        try {
                            Thread.sleep(myUntilCanCrawlAgainMs);
                        } catch (InterruptedException myException) {
                            System.err.println(myException);
                            break;
                        }
                    }
                }
            }

        }

        long myElapsedTimeMs = CrawlerUtils.getTimeElapsedSinceMs(myStartTimestampMs);
        System.out.printf(
                "[SerialBFSCrawler] returning %d discovered img srcs after pushing %d urls onto BFS queue in %d ms\n",
                myNotifier.getDiscoveredImgSrcs().size(),
                myNotifier.getAllSeenUrls().size(),
                myElapsedTimeMs);
        return new CrawlerResults.Builder(myCrawlerConfig)
                .withImgSrcs(myNotifier.getDiscoveredImgSrcs())
                .withCrawledUrls(myNotifier.getAllSeenUrls())
                .withCrawlTimeMs(myElapsedTimeMs)
                .build();
    }
}
