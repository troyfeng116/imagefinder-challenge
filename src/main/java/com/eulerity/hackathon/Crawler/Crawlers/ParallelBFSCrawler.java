package com.eulerity.hackathon.Crawler.Crawlers;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.eulerity.hackathon.Crawler.Crawler;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerResults;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Crawler.Notifiers.ThreadSafeCrawlerNotifier;
import com.eulerity.hackathon.Scraper.Scraper;

import crawlercommons.robots.SimpleRobotRules;

public class ParallelBFSCrawler extends Crawler {
    public ParallelBFSCrawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules) {
        super(aCrawlerConfig, aRobotRules);
    }

    @Override
    public CrawlerResults crawlAndScrape() {
        CrawlerConfig myCrawlerConfig = getCrawlerConfig();
        long myStartTimestampMs = System.currentTimeMillis();
        CrawlerNotifier myNotifier = new ThreadSafeCrawlerNotifier(myCrawlerConfig.getMaxImgSrcs(),
                myCrawlerConfig.getMaxUrls(), getRobotRules());

        URL myStartUrl = myCrawlerConfig.getStartUrl();
        String myStartUrlString = myStartUrl.toString();
        myNotifier.checkAndNotifyHref(myStartUrlString);

        Queue<String> myLevelUrls = new LinkedList<>();
        for (int myLevel = 0; myLevel < myCrawlerConfig.getMaxDepth()
                && myNotifier.drainNextUrlsQueue(myLevelUrls) > 0; myLevel++) {
            System.out.printf("scraping level=%d, %d new urls, %d seen urls\n", myLevel,
                    myLevelUrls.size(), myNotifier.getAllSeenUrls().size());
            int myLevelSz = myLevelUrls.size();

            CountDownLatch myLatch = new CountDownLatch(myLevelSz);
            ExecutorService myExecutorService = new ThreadPoolExecutor(8, 16, 5, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());
            while (!myLevelUrls.isEmpty()) {
                String myUrlToScrape = myLevelUrls.poll();
                myExecutorService.execute(() -> {
                    Scraper.scrape(myUrlToScrape, myCrawlerConfig.getShouldIncludeSVGs(),
                            myCrawlerConfig.getShouldIncludePNGs(), myNotifier);
                    myLatch.countDown();
                });
            }

            System.out.printf("awaiting latch for level %d with %d urls executing\n", myLevel, myLevelSz);
            try {
                myLatch.await(5, TimeUnit.SECONDS);
                myExecutorService.shutdownNow();
            } catch (InterruptedException myException) {
                System.err.println(myException);
            }
        }

        long myElapsedTimeMs = System.currentTimeMillis() - myStartTimestampMs;
        System.out.printf("[parallel] returning %d discovered img srcs after pushing %d urls onto BFS queue in %d ms\n",
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
