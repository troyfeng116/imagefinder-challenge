package com.eulerity.hackathon.Crawler.Notifiers;

import java.util.Collection;
import java.util.List;

import crawlercommons.robots.SimpleRobotRules;

public abstract class CrawlerNotifier {
    private final int theMaxImgSrcs;
    private final int theMaxUrls;
    private final SimpleRobotRules theRobotRules;

    public CrawlerNotifier(int aMaxImgSrcs, int aMaxUrls, SimpleRobotRules aRobotRules) {
        theMaxImgSrcs = aMaxImgSrcs;
        theMaxUrls = aMaxUrls;
        theRobotRules = aRobotRules;
    }

    public final int getMaxImgSrcs() {
        return theMaxImgSrcs;
    }

    public final int getMaxUrls() {
        return theMaxUrls;
    }

    public boolean checkAndNotifyHref(String aHref) {
        if (!theRobotRules.isAllowed(aHref)) {
            System.out.printf("[CrawlerNotifier] skipping href %s: robot rules not allowed\n", aHref);
            return true;
        }

        return notifyHref(aHref);
    }

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
    public abstract boolean notifyImgSrc(String aImgSrc);

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
    public abstract boolean notifyHref(String aHref);

    /**
     * Atomically drain all queued URLs for crawling from internal queued URLs.
     *
     * @return Number of atomically drained URLs to be crawled.
     */
    public abstract int drainNextUrlsQueue(Collection<String> aDrainTo);

    /**
     * Return all discovered img srcs during crawl.
     * Read operation may not be thread safe; recommended to allow all scraper
     * threads to join/finish notifying.
     *
     * @return Current discovered img srcs state.
     */
    public abstract List<String> getDiscoveredImgSrcs();

    /**
     * Return all queued hrefs during crawl. Note this includes hrefs that are
     * waiting on the queue, but haven't yet been visited/crawled.
     * Read operation may not be thread safe; recommended to allow all scraper
     * threads to join/finish notifying.
     * 
     * @return All visited/encountered and queued hrefs during crawl.
     */
    public abstract List<String> getAllSeenUrls();
}
