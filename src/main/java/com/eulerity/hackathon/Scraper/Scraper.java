package com.eulerity.hackathon.Scraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerUtils;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

/**
 * Responsible for:
 * - network I/O (retrieve page via http)
 * - respects maxCrawlTimeMs specified in CrawlerConfig via connection timeout
 * - handles non-200 status codes according to retry policy
 * - scraping with `jsoup`, extracting image srcs and neighboring link hrefs
 * - processing all srcs/urls (reconstructing relative paths, adding protocols)
 * - filtering URLs (avoid leaving domain, optionally avoid SVG/PNG formats)
 * - reporting all image srcs and link hrefs to notifier
 * 
 * @return `true` iff an HTTP GET request was successfully made.
 */
public class Scraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scraper.class);

    public static boolean scrape(String aUrlString, CrawlerConfig aCrawlerConfig,
            CrawlerNotifier aNotifier, RetryPolicy aRetryPolicy, long aTimeLimitMs) {
        URL myUrl;
        try {
            myUrl = new URL(aUrlString);
        } catch (MalformedURLException myException) {
            LOGGER.error(myException.getMessage());
            return false;
        }

        Document myDocument = attemptToGetHtmlWithRetry(myUrl, aRetryPolicy, aTimeLimitMs);
        if (myDocument == null) {
            return false;
        }

        String myDomain = myUrl.getHost();

        Elements myAnchorElements = myDocument.getElementsByTag("a");
        List<String> myAnchorHrefs = myAnchorElements.stream()
                .map((myElement) -> myElement.attr("href"))
                .filter(myHref -> myHref != null && myHref.length() > 0)
                .map(myHref -> getFullUrl(myUrl, myHref))
                .filter(myHref -> myHref.contains(myDomain))
                .collect(Collectors.toList());

        Elements myImgElements = myDocument.getElementsByTag("img");
        List<String> myImgSrcs = myImgElements.stream()
                .map((myElement) -> myElement.attr("src"))
                .filter(mySrc -> mySrc != null && mySrc.length() > 0)
                .filter(mySrc -> aCrawlerConfig.getShouldIncludeSVGs() ? true : !mySrc.contains(".svg"))
                .filter(mySrc -> aCrawlerConfig.getShouldIncludePNGs() ? true : !mySrc.contains(".png"))
                .map(mySrc -> getFullUrl(myUrl, mySrc))
                .collect(Collectors.toList());

        LOGGER.debug(String.format("scraped %s, found %d img src and %d neighbor urls",
                myUrl.toString(),
                myImgSrcs.size(),
                myAnchorHrefs.size()));

        // `allMatch`: lazy streams stop iteration once `apply` fails
        myImgSrcs.stream().allMatch(aNotifier::notifyImgSrc);
        myAnchorHrefs.stream().allMatch(aNotifier::checkAndNotifyHref);
        return true;
    }

    /**
     * Attempts to get HTML document for jsoup scraping via HTTP GET. Retries
     * according to `aRetryPolicy`.
     * 
     * @param aUrl         HTTP/HTTPS URL for jsoup connection.
     * @param aRetryPolicy Retry policy incase of retry-able http status codes.
     * @return `Document` object if 200 status code response, `null` if no
     *         successful tries.
     */
    private static Document attemptToGetHtmlWithRetry(URL aUrl, RetryPolicy aRetryPolicy, long aTimeLimitMs) {
        long myStartTimestampMs = System.currentTimeMillis();
        Document myDocument = null;
        while (myDocument == null) {
            try {
                Connection myConnection = Jsoup.connect(aUrl.toString());
                int myTimeRemainingMs = Math.max(1,
                        (int) (aTimeLimitMs - CrawlerUtils.getTimeElapsedSinceMs(myStartTimestampMs)));
                Connection.Response myResponse = myConnection
                        .timeout(myTimeRemainingMs)
                        .execute();
                myDocument = myResponse.parse();
                int myStatusCode = myConnection.response().statusCode();
                if (myStatusCode == 200) {
                    break;
                }

                if (myStatusCode == 500 || myStatusCode == 502 || myStatusCode == 503 || myStatusCode == 504) {
                    long myRetryDelayMs = aRetryPolicy.getNextRetryMs();
                    if (myRetryDelayMs == RetryPolicy.DO_NOT_RETRY) {
                        LOGGER.debug(String.format("no longer retrying for status %d\n", myStatusCode));
                        return null;
                    }

                    // retry
                    LOGGER.debug(String.format("retrying for status %d after %d ms sleep\n", myStatusCode,
                            myRetryDelayMs));
                    myDocument = null;
                    Thread.sleep(myRetryDelayMs);
                } else {
                    return null;
                }
            } catch (Exception myException) {
                LOGGER.error(myException.getMessage());
                return null;
            }
        }

        return myDocument;
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
    private static String getFullUrl(URL aUrl, String aSrc) {
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
