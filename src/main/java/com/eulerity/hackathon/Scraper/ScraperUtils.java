package com.eulerity.hackathon.Scraper;

import java.net.URL;

public class ScraperUtils {
    private ScraperUtils() {
    }

    /**
     * Formats raw src or href: handles relative paths by appending base domain, and
     * appends protocol if necessary.
     *
     * @param aUrl Original url from which src was scraped.
     * @param aSrc Raw src or href attribute (might be relative path, incomplete
     *             address, etc.).
     * @return Full address corresponding to src, including protocol and domain.
     */
    protected static String getFullUrl(URL aUrl, String aSrc) {
        String myLowerSrc = aSrc.toLowerCase();
        if (myLowerSrc.startsWith("https") || myLowerSrc.startsWith("http")) {
            return aSrc;
        }
        if (aSrc.startsWith("//")) {
            return aUrl.getProtocol() + ':' + aSrc;
        }

        String myBaseDomain = aUrl.getProtocol() + "://" + aUrl.getHost();
        return myBaseDomain + (aSrc.startsWith("/") ? "" : "/") + aSrc;
    }
}
