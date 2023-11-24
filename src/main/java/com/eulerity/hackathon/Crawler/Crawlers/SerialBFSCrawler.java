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

import crawlercommons.robots.SimpleRobotRules;

public class SerialBFSCrawler extends Crawler {
    public SerialBFSCrawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules) {
        super(aCrawlerConfig, aRobotRules);
    }

    @Override
    public CrawlerResults crawlAndScrape() {
        CrawlerConfig myCrawlerConfig = getCrawlerConfig();
        long myStartTimestampMs = System.currentTimeMillis();
        CrawlerNotifier myNotifier = new SerialCrawlerNotifier(myCrawlerConfig.getMaxImgSrcs(),
                myCrawlerConfig.getMaxUrls(), getRobotRules());

        URL myStartUrl = myCrawlerConfig.getStartUrl();
        String myStartUrlString = myStartUrl.toString();
        myNotifier.checkAndNotifyHref(myStartUrlString);

        Queue<String> myLevelUrls = new LinkedList<>();
        for (int myLevel = 0; myLevel < myCrawlerConfig.getMaxDepth()
                && myNotifier.drainNextUrlsQueue(myLevelUrls) > 0; myLevel++) {
            System.out.printf("scraping level=%d, %d new urls, %d seen urls\n", myLevel,
                    myLevelUrls.size(), myNotifier.getAllSeenUrls().size());

            while (!myLevelUrls.isEmpty()
                    && myNotifier.getDiscoveredImgSrcs().size() < myCrawlerConfig
                            .getMaxImgSrcs()) {
                String myUrlToScrape = myLevelUrls.poll();
                Scraper.scrape(myUrlToScrape, myCrawlerConfig.getShouldIncludeSVGs(),
                        myCrawlerConfig.getShouldIncludePNGs(), myNotifier);
            }
        }

        long myElapsedTimeMs = System.currentTimeMillis() - myStartTimestampMs;
        System.out.printf(
                "[serial] returning %d discovered img srcs after pushing %d urls onto BFS queue in %d ms\n",
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
