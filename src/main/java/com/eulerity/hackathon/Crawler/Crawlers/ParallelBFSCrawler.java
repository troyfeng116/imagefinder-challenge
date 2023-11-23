package com.eulerity.hackathon.Crawler.Crawlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.eulerity.hackathon.Crawler.Crawler;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerResults;
import com.eulerity.hackathon.Scraper.Scraper;

public class ParallelBFSCrawler implements Crawler {
    private final CrawlerConfig theCrawlerConfig;

    public ParallelBFSCrawler(CrawlerConfig aCrawlerConfig) {
        theCrawlerConfig = aCrawlerConfig;
    }

    @Override
    public CrawlerResults crawlAndScrape() {
        long myStartTimestampMs = System.currentTimeMillis();
        Map<String, Boolean> myDiscoveredImageSrcs = new ConcurrentHashMap<>();
        Map<String, Boolean> mySeenUrls = new ConcurrentHashMap<>();
        Queue<String> myNextUrlsToScrape = new ConcurrentLinkedQueue<>();

        URL myStartUrl = theCrawlerConfig.getStartUrl();
        String myStartUrlString = myStartUrl.toString();
        myNextUrlsToScrape.offer(myStartUrlString);
        mySeenUrls.put(myStartUrlString, true);

        for (int myLevel = 0; myLevel < theCrawlerConfig.getMaxDepth() && !myNextUrlsToScrape.isEmpty(); myLevel++) {
            System.out.printf("scraping depth=%d, %d new urls, %d seen urls\n", theCrawlerConfig.getMaxDepth(),
                    myNextUrlsToScrape.size(), mySeenUrls.size());

            // drain all URLs from synch queue
            Queue<String> myLevelUrls = new LinkedList<>();
            while (!myNextUrlsToScrape.isEmpty()) {
                myLevelUrls.offer(myNextUrlsToScrape.poll());
            }
            int myLevelSz = myLevelUrls.size();

            CountDownLatch myLatch = new CountDownLatch(myLevelSz);
            ExecutorService myExecutorService = new ThreadPoolExecutor(8, 16, 5, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());
            while (!myLevelUrls.isEmpty()) {
                String myUrlToScrape = myLevelUrls.poll();
                myExecutorService.execute(() -> {
                    Scraper.scrape(myUrlToScrape,
                            (aImgSrc) -> {
                                // synchronization: computeIfAbsent atomicity
                                Boolean myComputeResult = myDiscoveredImageSrcs.computeIfAbsent(aImgSrc, (__) -> {
                                    return myDiscoveredImageSrcs.size() < theCrawlerConfig.getMaxImgSrcs() ? true
                                            : null;
                                });
                                return myComputeResult != null;
                            },
                            (aNeighborUrl) -> {
                                // synchronization: computeIfAbsent atomic, atomic get-set unlocks right to add
                                // to BFS queue
                                Boolean myComputeResult = mySeenUrls.computeIfAbsent(aNeighborUrl, (__) -> {
                                    if (mySeenUrls.size() < theCrawlerConfig.getMaxUrls()) {
                                        myNextUrlsToScrape.offer(aNeighborUrl);
                                        return true;
                                    }
                                    return null;
                                });
                                return myComputeResult != null;
                            });
                    myLatch.countDown();
                });
            }

            System.out.printf("awaiting latch for level %d with %d urls executing\n", myLevel, myLevelSz);
            try {
                myLatch.await(5, TimeUnit.SECONDS);
                myExecutorService.shutdownNow();
            } catch (InterruptedException myException) {
                System.err.println(myException);
                return new CrawlerResults.Builder(theCrawlerConfig).build();
            }
        }

        System.out.printf("returning %d discovered img srcs after pushing %d urls onto BFS queue\n",
                myDiscoveredImageSrcs.size(),
                mySeenUrls.size());
        return new CrawlerResults.Builder(theCrawlerConfig)
                .withImgSrcs(new ArrayList<>(myDiscoveredImageSrcs.keySet()))
                .withCrawledUrls(new ArrayList<>(mySeenUrls.keySet()))
                .withCrawlTimeMs(System.currentTimeMillis() - myStartTimestampMs)
                .build();
    }
}
