package com.eulerity.hackathon.Crawler.Crawlers;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eulerity.hackathon.Crawler.Crawler;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerUtils;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifierFactory;
import com.eulerity.hackathon.Scraper.Scraper;
import com.eulerity.hackathon.Scraper.DocumentFetcher.DefaultDocumentFetcher;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

import crawlercommons.robots.SimpleRobotRules;

public class SerialBFSCrawler extends Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialBFSCrawler.class);

    public SerialBFSCrawler(CrawlerConfig aCrawlerConfig, SimpleRobotRules aRobotRules, RetryPolicy aRetryPolicy) {
        super(aCrawlerConfig, aRobotRules, aRetryPolicy,
                CrawlerNotifierFactory.create(true,
                        aCrawlerConfig.getMaxImgSrcs(), aCrawlerConfig.getMaxUrls(), aRobotRules));
    }

    @Override
    public boolean handleCrawlLevel(long aStartTimestampMs, int aLevel, Queue<String> aLevelUrls) {
        Scraper myScraper = getScraper();
        CrawlerNotifier myCrawlerNotifier = getCrawlerNotifier();
        CrawlerConfig myCrawlerConfig = getCrawlerConfig();
        long myRobotsCrawlDelayMs = getRobotRules().getCrawlDelay() * 1000;
        long myRemainingCrawlTimeMs;
        while (!aLevelUrls.isEmpty()
                && myCrawlerNotifier.getDiscoveredImgSrcs().size() < myCrawlerConfig.getMaxImgSrcs()
                && (myRemainingCrawlTimeMs = myCrawlerConfig.getMaxCrawlTimeMs()
                        - CrawlerUtils.getTimeElapsedSinceMs(aStartTimestampMs)) > 0) {
            long myUrlStartTimestampMs = System.currentTimeMillis();
            String myUrlToScrape = aLevelUrls.poll();
            if (myScraper.scrape(myUrlToScrape, myCrawlerConfig, myCrawlerNotifier, getRetryPolicy(),
                    myRemainingCrawlTimeMs, DefaultDocumentFetcher.INSTANCE)) {
                // serial crawler used for sites with robots.txt crawl-delay specified
                // -> sleep until delay elapses if needed
                long myUrlElapsedTimeMs = CrawlerUtils.getTimeElapsedSinceMs(myUrlStartTimestampMs);
                long myUntilCanCrawlAgainMs = myRobotsCrawlDelayMs - myUrlElapsedTimeMs;
                if (myUntilCanCrawlAgainMs > 0) {
                    myRemainingCrawlTimeMs = myCrawlerConfig.getMaxCrawlTimeMs()
                            - CrawlerUtils.getTimeElapsedSinceMs(aStartTimestampMs);
                    // don't bother scraping again if remaining crawl time not long enough for
                    // robots.txt crawl delay
                    if (myUntilCanCrawlAgainMs >= myRemainingCrawlTimeMs) {
                        return false;
                    }

                    LOGGER.info(String.format(
                            "scraped url %s: sleeping for %d ms after scrape time %d ms to respect robots.txt crawl delay %d ms",
                            myUrlToScrape, myUntilCanCrawlAgainMs, myUrlElapsedTimeMs, myRobotsCrawlDelayMs));
                    try {
                        Thread.sleep(myUntilCanCrawlAgainMs);
                    } catch (InterruptedException myException) {
                        LOGGER.error(myException.getMessage());
                        break;
                    }
                }
            }
        }

        return true;
    }
}
