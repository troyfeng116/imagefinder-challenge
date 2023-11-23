package com.eulerity.hackathon.Crawler.Notifiers;

import java.util.Collection;
import java.util.List;

public interface CrawlerNotifier {
    /**
     * Scrapers invoke upon finding a new img src URL during crawl.
     *
     * @param aImgSrc Newly scraped and discovered img src URL.
     * @return `false` iff the new img src URL is not accepted: that is, iff the
     *         newly notified img src has not been discovered before, but the
     *         current crawl has exceeded max img src limits, and adding the new img
     *         src would exceed limits.
     *         Used to help scrapers optimistically end iteration through new img
     *         srcs during scraping/notification.
     */
    boolean notifyImgSrc(String aImgSrc);

    /**
     * Scrapers invoke upon finding a new link href to a new page during crawl.
     *
     * @param aHref Newly scraped and discovered anchor href.
     * @return `false` iff the new link href is not accepted: that is, iff the newly
     *         notified link href has not been crawled/queued to crawl before, but
     *         the current crawl has exceeded max URL limits, and adding the new
     *         href would exceed limits.
     *         Used to help scrapers optimistically end iteration through new hrefs
     *         during scraping/notification.
     */
    boolean notifyHref(String aHref);

    /**
     * Atomically drain all queued URLs for crawling from internal queued URLs.
     *
     * @return Number of atomically drained URLs to be crawled.
     */
    int drainNextUrlsQueue(Collection<String> aDrainTo);

    /**
     * Return all discovered img srcs during crawl.
     * Read operation may not be thread safe; recommended to allow all scraper
     * threads to join/finish notifying.
     *
     * @return Current discovered img srcs state.
     */
    List<String> getDiscoveredImgSrcs();

    /**
     * Return all queued hrefs during crawl. Note this includes hrefs that are
     * waiting on the queue, but haven't yet been visited/crawled.
     * Read operation may not be thread safe; recommended to allow all scraper
     * threads to join/finish notifying.
     * 
     * @return All encountered and queued hrefs during crawl.
     */
    List<String> getQueuedUrls();
}
