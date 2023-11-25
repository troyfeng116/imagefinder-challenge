package com.eulerity.hackathon.Crawler.Crawlers;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import com.eulerity.hackathon.Crawler.Crawler;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerResults;
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
        for (int myLevel = 0; myLevel < myCrawlerConfig.getMaxDepth()
                && myNotifier.drainNextUrlsQueue(myLevelUrls) > 0; myLevel++) {
            System.out.printf("[SerialBFSCrawler] scraping level=%d, %d new urls, %d seen urls\n", myLevel,
                    myLevelUrls.size(), myNotifier.getAllSeenUrls().size());

            while (!myLevelUrls.isEmpty()
                    && myNotifier.getDiscoveredImgSrcs().size() < myCrawlerConfig.getMaxImgSrcs()) {
                long myUrlStartTimestampMs = System.currentTimeMillis();
                String myUrlToScrape = myLevelUrls.poll();
                if (Scraper.scrape(myUrlToScrape, myCrawlerConfig.getShouldIncludeSVGs(),
                        myCrawlerConfig.getShouldIncludePNGs(), myNotifier, getRetryPolicy())) {
                    // serial crawler used for sites with robots.txt crawl-delay specified
                    // -> sleep until delay elapses if needed
                    long myUrlElapsedTimeMs = System.currentTimeMillis() - myUrlStartTimestampMs;
                    long myUntilCanCrawlAgainMs = myRobotsCrawlDelayMs - myUrlElapsedTimeMs;
                    if (myUntilCanCrawlAgainMs > 0) {
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

        long myElapsedTimeMs = System.currentTimeMillis() - myStartTimestampMs;
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
