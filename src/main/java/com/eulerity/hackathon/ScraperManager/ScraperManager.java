package com.eulerity.hackathon.ScraperManager;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.eulerity.hackathon.Scraper.ScrapeResults;
import com.eulerity.hackathon.Scraper.Scraper;

// contract: one manager per request (one parent thread, with separate object, instead of static fields)
public class ScraperManager {
    private final String theStartUrl;
    private final int theMaxDepth;
    private final int theMaxSrcs;
    private final int theMaxUrls;

    public ScraperManager(String aStartUrl, int aMaxDepth, int aMaxSrcs, int aMaxUrls) {
        theStartUrl = aStartUrl;
        theMaxDepth = aMaxDepth;
        theMaxSrcs = aMaxSrcs;
        theMaxUrls = aMaxUrls;
    }

    public Set<String> crawlAndScrape() {
        Map<String, Boolean> myDiscoveredImageSrcs = new ConcurrentHashMap<>();
        Map<String, Boolean> mySeenUrls = new ConcurrentHashMap<>();
        Queue<String> myNextUrlsToScrape = new ConcurrentLinkedQueue<>();

        myNextUrlsToScrape.offer(theStartUrl);
        mySeenUrls.put(theStartUrl, true);

        for (int myLevel = 0; myLevel < theMaxDepth && !myNextUrlsToScrape.isEmpty(); myLevel++) {
            System.out.printf("scraping depth=%d, %d new urls, %d seen urls\n", theMaxDepth,
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
                                    return myDiscoveredImageSrcs.size() < theMaxSrcs ? true : null;
                                });
                                return myComputeResult != null;
                            },
                            (aNeighborUrl) -> {
                                // synchronization: computeIfAbsent atomic, atomic get-set unlocks right to add
                                // to BFS queue
                                Boolean myComputeResult = mySeenUrls.computeIfAbsent(aNeighborUrl, (__) -> {
                                    if (mySeenUrls.size() < theMaxUrls) {
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
            } catch (InterruptedException myException) {
                System.err.println(myException);
                return new HashSet<>(mySeenUrls.keySet());
            }
        }

        System.out.printf("returning %d discovered img srcs after pushing %d urls onto BFS queue\n",
                myDiscoveredImageSrcs.size(),
                mySeenUrls.size());
        return new HashSet<>(myDiscoveredImageSrcs.keySet());
    }
}
