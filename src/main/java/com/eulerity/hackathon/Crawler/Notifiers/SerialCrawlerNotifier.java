package com.eulerity.hackathon.Crawler.Notifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SerialCrawlerNotifier extends CrawlerNotifier {
    private final Set<String> theSeenUrls;
    private final Set<String> theDiscoveredImageSrcs;
    private final Queue<String> theQueuedUrls;

    public SerialCrawlerNotifier(int aMaxImgSrcs, int aMaxUrls) {
        super(aMaxImgSrcs, aMaxUrls);

        theSeenUrls = new HashSet<>();
        theDiscoveredImageSrcs = new HashSet<>();
        theQueuedUrls = new LinkedList<>();
    }

    @Override
    public boolean notifyImgSrc(String aImgSrc) {
        if (theDiscoveredImageSrcs.size() > getMaxImgSrcs()) {
            return false;
        }

        theDiscoveredImageSrcs.add(aImgSrc);
        return true;
    }

    @Override
    public boolean notifyHref(String aHref) {
        if (theSeenUrls.size() > getMaxUrls() || theDiscoveredImageSrcs.size() > getMaxImgSrcs()) {
            return false;
        }

        if (theSeenUrls.add(aHref)) {
            theQueuedUrls.offer(aHref);
        }
        return true;
    }

    @Override
    public int drainNextUrlsQueue(Collection<String> aDrainTo) {
        int myQueueSize = theQueuedUrls.size();
        while (!theQueuedUrls.isEmpty()) {
            aDrainTo.add(theQueuedUrls.poll());
        }
        return myQueueSize;
    }

    @Override
    public List<String> getDiscoveredImgSrcs() {
        return new ArrayList<>(theDiscoveredImageSrcs);
    }

    @Override
    public List<String> getAllSeenUrls() {
        return new ArrayList<>(theSeenUrls);
    }
}
