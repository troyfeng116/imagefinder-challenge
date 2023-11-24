package com.eulerity.hackathon.Crawler.Notifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadSafeCrawlerNotifier extends CrawlerNotifier {
    private final Map<String, Boolean> theSeenUrls;
    private final Map<String, Boolean> theDiscoveredImageSrcs;
    private final Queue<String> theQueuedUrls;

    public ThreadSafeCrawlerNotifier(int aMaxImgSrcs, int aMaxUrls) {
        super(aMaxImgSrcs, aMaxUrls);

        theSeenUrls = new ConcurrentHashMap<>();
        theDiscoveredImageSrcs = new ConcurrentHashMap<>();
        theQueuedUrls = new ConcurrentLinkedQueue<>();
    }

    @Override
    public List<String> getDiscoveredImgSrcs() {
        return new ArrayList<>(theDiscoveredImageSrcs.keySet());
    }

    @Override
    public List<String> getAllSeenUrls() {
        return new ArrayList<>(theSeenUrls.keySet());
    }

    @Override
    public synchronized int drainNextUrlsQueue(Collection<String> aDrainTo) {
        int myDrainedCt = 0;
        while (!theQueuedUrls.isEmpty()) {
            aDrainTo.add(theQueuedUrls.poll());
            myDrainedCt++;
        }
        return myDrainedCt;
    }

    @Override
    public boolean notifyImgSrc(String aImgSrc) {
        // synchronization: computeIfAbsent atomicity
        Boolean myComputeResult = theDiscoveredImageSrcs.computeIfAbsent(aImgSrc, (__) -> {
            return theDiscoveredImageSrcs.size() < getMaxImgSrcs() ? true
                    : null;
        });
        return myComputeResult != null;
    }

    @Override
    public boolean notifyHref(String aHref) {
        // synchronization: computeIfAbsent atomic: seenUrls size guaranteed to be
        // minimal, acts as lock around theNextUrlsToScrape
        Boolean myComputeResult = theSeenUrls.computeIfAbsent(aHref, (__) -> {
            if (theSeenUrls.size() < getMaxUrls()) {
                theQueuedUrls.offer(aHref);
                return true;
            }
            return null;
        });
        return myComputeResult != null;
    }
}
