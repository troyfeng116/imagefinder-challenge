package com.eulerity.hackathon.Scraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Scraper.DocumentFetcher.DocumentFetcher;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

/**
 * Responsible for:
 * - checking URL string (i.e. URL-parse-able, accepted by `Jsoup.connect`)
 * - using provided `DocumentFetcher` to retrieve HTML
 * - scraping with `jsoup`, extracting image srcs and neighboring link hrefs
 * - processing all srcs/urls (reconstructing relative paths, adding protocols)
 * - filtering URLs (avoid leaving domain, optionally avoid SVG/PNG formats)
 * - reporting all image srcs and link hrefs to notifier
 * 
 * @return `true` iff an HTTP GET request was successfully made.
 */
public class Scraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scraper.class);

    public boolean scrape(String aUrlString, CrawlerConfig aCrawlerConfig,
            CrawlerNotifier aNotifier, RetryPolicy aRetryPolicy, long aTimeLimitMs, DocumentFetcher aDocumentFetcher) {
        URL myUrl;
        try {
            myUrl = new URL(aUrlString);
        } catch (MalformedURLException myException) {
            LOGGER.error(myException.getMessage());
            return false;
        }

        Document myDocument = aDocumentFetcher.attemptToGetHtmlWithRetry(myUrl.toString(), aRetryPolicy, aTimeLimitMs,
                Jsoup::connect);
        if (myDocument == null) {
            return false;
        }

        String myDomain = myUrl.getHost();

        Elements myAnchorElements = myDocument.getElementsByTag("a");
        List<String> myAnchorHrefs = myAnchorElements.stream()
                .map((myElement) -> myElement.attr("href"))
                .filter(myHref -> myHref != null && myHref.length() > 0)
                .map(myHref -> ScraperUtils.getFullUrl(myUrl, myHref))
                .filter(myHref -> myHref.contains(myDomain))
                .collect(Collectors.toList());

        Elements myImgElements = myDocument.getElementsByTag("img");
        List<String> myImgSrcs = myImgElements.stream()
                .map((myElement) -> myElement.attr("src"))
                .filter(mySrc -> mySrc != null && mySrc.length() > 0)
                .filter(mySrc -> aCrawlerConfig.getShouldIncludeSVGs() ? true : !mySrc.contains(".svg"))
                .filter(mySrc -> aCrawlerConfig.getShouldIncludePNGs() ? true : !mySrc.contains(".png"))
                .map(mySrc -> ScraperUtils.getFullUrl(myUrl, mySrc))
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
}
