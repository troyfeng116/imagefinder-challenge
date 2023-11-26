package com.eulerity.hackathon.Crawler.Notifiers;

import crawlercommons.robots.SimpleRobotRules;

public class CrawlerNotifierFactory {
    private CrawlerNotifierFactory() {
    }

    public static CrawlerNotifier create(boolean aShouldBeThreadSafe, int aMaxImgSrcs, int aMaxUrls,
            SimpleRobotRules aRobotRules) {
        return aShouldBeThreadSafe ? new ThreadSafeCrawlerNotifier(aMaxImgSrcs, aMaxUrls, aRobotRules)
                : new SerialCrawlerNotifier(aMaxImgSrcs, aMaxUrls, aRobotRules);
    }
}
