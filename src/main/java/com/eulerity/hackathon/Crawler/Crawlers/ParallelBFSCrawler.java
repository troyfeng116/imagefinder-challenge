package com.eulerity.hackathon.Crawler.Crawlers;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eulerity.hackathon.Crawler.Crawler;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerUtils;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifierFactory;
import com.eulerity.hackathon.Scraper.Scraper;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

import crawlercommons.robots.SimpleRobotRules;

public class ParallelBFSCrawler extends Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelBFSCrawler.class);

    public ParallelBFSCrawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules, RetryPolicy aRetryPolicy) {
        super(aCrawlerConfig, aRobotRules, aRetryPolicy,
                CrawlerNotifierFactory.create(true, aCrawlerConfig.getMaxImgSrcs(), aCrawlerConfig.getMaxUrls(),
                        aRobotRules));
    }

    @Override
    public boolean handleCrawlLevel(long aStartTimestampMs, int aLevel, Queue<String> aLevelUrls) {
        CrawlerNotifier myCrawlerNotifier = getCrawlerNotifier();
        CrawlerConfig myCrawlerConfig = getCrawlerConfig();
        int myLevelSz = aLevelUrls.size();

        CountDownLatch myLatch = new CountDownLatch(myLevelSz);
        long myExecutorServiceTimeoutMs = Math.max(0,
                myCrawlerConfig.getMaxCrawlTimeMs() - CrawlerUtils.getTimeElapsedSinceMs(aStartTimestampMs));
        ExecutorService myExecutorService = new ThreadPoolExecutor(8, 16, myExecutorServiceTimeoutMs,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        while (!aLevelUrls.isEmpty()) {
            String myUrlToScrape = aLevelUrls.poll();
            myExecutorService.execute(() -> {
                Scraper.scrape(myUrlToScrape, myCrawlerConfig, myCrawlerNotifier, getRetryPolicy(),
                        myExecutorServiceTimeoutMs);
                myLatch.countDown();
            });
        }

        long myLatchTimeoutMs = Math.max(0,
                myCrawlerConfig.getMaxCrawlTimeMs() - CrawlerUtils.getTimeElapsedSinceMs(aStartTimestampMs));
        LOGGER.info(String.format("awaiting latch for level %d with %d urls executing", aLevel, myLevelSz));
        try {
            myLatch.await(myLatchTimeoutMs, TimeUnit.MILLISECONDS);
            myExecutorService.shutdownNow();
        } catch (InterruptedException myException) {
            LOGGER.error(myException.getMessage());
        }

        return true;
    }
}
