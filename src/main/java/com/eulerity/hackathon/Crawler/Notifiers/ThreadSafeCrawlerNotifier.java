package com.eulerity.hackathon.Crawler.Notifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadSafeCrawlerNotifier implements CrawlerNotifier {
    private final Map<String, Boolean> theQueuedUrls;
    private final Map<String, Boolean> theDiscoveredImageSrcs;
    private final Queue<String> theNextUrlsToScrape;
    private final int theMaxImgSrcs;
    private final int theMaxUrls;

    public ThreadSafeCrawlerNotifier(int aMaxImgSrcs, int aMaxUrls) {
        theQueuedUrls = new ConcurrentHashMap<>();
        theDiscoveredImageSrcs = new ConcurrentHashMap<>();
        theNextUrlsToScrape = new ConcurrentLinkedQueue<>();
        theMaxImgSrcs = aMaxImgSrcs;
        theMaxUrls = aMaxUrls;
    }

    @Override
    public List<String> getDiscoveredImgSrcs() {
        return new ArrayList<>(theDiscoveredImageSrcs.keySet());
    }

    @Override
    public List<String> getQueuedUrls() {
        return new ArrayList<>(theQueuedUrls.keySet());
    }

    @Override
    public synchronized int drainNextUrlsQueue(Collection<String> aDrainTo) {
        int myDrainedCt = 0;
        while (!theNextUrlsToScrape.isEmpty()) {
            aDrainTo.add(theNextUrlsToScrape.poll());
            myDrainedCt++;
        }
        return myDrainedCt;
    }

    @Override
    public boolean notifyImgSrc(String aImgSrc) {
        // synchronization: computeIfAbsent atomicity
        Boolean myComputeResult = theDiscoveredImageSrcs.computeIfAbsent(aImgSrc, (__) -> {
            return theDiscoveredImageSrcs.size() < theMaxImgSrcs ? true
                    : null;
        });
        return myComputeResult != null;
    }

    @Override
    public boolean notifyHref(String aHref) {
        // synchronization: computeIfAbsent atomic: seenUrls size guaranteed to be
        // minimal, acts as lock around theNextUrlsToScrape
        Boolean myComputeResult = theQueuedUrls.computeIfAbsent(aHref, (__) -> {
            if (theQueuedUrls.size() < theMaxUrls) {
                theNextUrlsToScrape.offer(aHref);
                return true;
            }
            return null;
        });
        return myComputeResult != null;
    }
}
