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

public class SerialBFSCrawler implements Crawler {
    private final CrawlerConfig theCrawlerConfig;

    public SerialBFSCrawler(CrawlerConfig aCrawlerConfig) {
        theCrawlerConfig = aCrawlerConfig;
    }

    @Override
    public CrawlerResults crawlAndScrape() {
        long myStartTimestampMs = System.currentTimeMillis();
        CrawlerNotifier myNotifier = new SerialCrawlerNotifier(theCrawlerConfig.getMaxImgSrcs(),
                theCrawlerConfig.getMaxUrls());

        URL myStartUrl = theCrawlerConfig.getStartUrl();
        String myStartUrlString = myStartUrl.toString();
        myNotifier.notifyHref(myStartUrlString);

        Queue<String> myLevelUrls = new LinkedList<>();
        for (int myLevel = 0; myLevel < theCrawlerConfig.getMaxDepth()
                && myNotifier.drainNextUrlsQueue(myLevelUrls) > 0; myLevel++) {
            System.out.printf("scraping level=%d, %d new urls, %d seen urls\n", myLevel,
                    myLevelUrls.size(), myNotifier.getAllSeenUrls().size());

            while (!myLevelUrls.isEmpty()) {
                String myUrlToScrape = myLevelUrls.poll();
                Scraper.scrape(myUrlToScrape, theCrawlerConfig.getShouldIncludeSVGs(),
                        theCrawlerConfig.getShouldIncludePNGs(), myNotifier);
            }
        }

        long myElapsedTimeMs = System.currentTimeMillis() - myStartTimestampMs;
        System.out.printf("[serial] returning %d discovered img srcs after pushing %d urls onto BFS queue in %d ms\n",
                myNotifier.getDiscoveredImgSrcs().size(),
                myNotifier.getAllSeenUrls().size(),
                myElapsedTimeMs);
        return new CrawlerResults.Builder(theCrawlerConfig)
                .withImgSrcs(myNotifier.getDiscoveredImgSrcs())
                .withCrawledUrls(myNotifier.getAllSeenUrls())
                .withCrawlTimeMs(myElapsedTimeMs)
                .build();
    }
}
