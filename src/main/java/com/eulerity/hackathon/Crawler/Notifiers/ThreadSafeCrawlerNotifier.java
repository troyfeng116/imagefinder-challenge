package com.eulerity.hackathon.Crawler.Notifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import crawlercommons.robots.SimpleRobotRules;

public class ThreadSafeCrawlerNotifier extends CrawlerNotifier {
    private final Map<String, Boolean> theSeenUrls;
    private final Map<String, Boolean> theDiscoveredImageSrcs;
    private final Queue<String> theQueuedUrls;

    public ThreadSafeCrawlerNotifier(int aMaxImgSrcs, int aMaxUrls, SimpleRobotRules aRobotRules) {
        super(aMaxImgSrcs, aMaxUrls, aRobotRules);

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
        // synchronization: computeIfAbsent atomic -> read to seenUrls guaranteed to be
        // consistent with size, so queuedUrls.offer happens iff atomic compute + read
        // size pass
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
