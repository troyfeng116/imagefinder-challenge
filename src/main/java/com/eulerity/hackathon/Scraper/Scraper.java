package com.eulerity.hackathon.Scraper;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;

/**
 * Responsible for:
 * - network I/O (retrieve page via http)
 * - scraping with `jsoup`, extracting image srcs and neighboring link hrefs
 * - processing all srcs/urls (reconstructing relative paths, adding protocols)
 * - filtering URLs (avoid leaving domain, optionally avoid SVG/PNG formats)
 * - reporting all image srcs and link hrefs to notifier
 * 
 * TODO:
 * - handle network I/O exceptions (retry policy)
 */
public class Scraper {
    public static void scrape(String aUrl, boolean aShouldIncludeSVGs, boolean aShouldIncludePNGs,
            CrawlerNotifier aNotifier) {
        try {
            URL myUrl = new URL(aUrl);
            String myDomain = myUrl.getHost();
            Connection myConnection = Jsoup.connect(aUrl);
            Connection.Response myResponse = myConnection.execute();
            Document myDocument = myResponse.parse();
            int myStatusCode = myConnection.response().statusCode();
            // System.out.println(myStatusCode);
            if (myStatusCode != 200) {
                // TODO: retry policy
            }

            Elements myAnchorElements = myDocument.getElementsByTag("a");
            List<String> myAnchorHrefs = myAnchorElements.stream()
                    .map((myElement) -> myElement.attr("href"))
                    .filter(myHref -> myHref != null && myHref.length() > 0)
                    .map(myHref -> getFullUrl(myUrl, myHref))
                    .filter(myHref -> myHref.contains(myDomain))
                    .collect(Collectors.toList());
            // System.out.println(myAnchorHrefs.stream().collect(Collectors.joining(",")));

            Elements myImgElements = myDocument.getElementsByTag("img");
            List<String> myImgSrcs = myImgElements.stream()
                    .map((myElement) -> myElement.attr("src"))
                    .filter(mySrc -> mySrc != null && mySrc.length() > 0)
                    .filter(mySrc -> aShouldIncludeSVGs ? true : !mySrc.contains(".svg"))
                    .filter(mySrc -> aShouldIncludePNGs ? true : !mySrc.contains(".png"))
                    .map(mySrc -> getFullUrl(myUrl, mySrc))
                    .collect(Collectors.toList());
            // System.out.println(myImgSrcs.stream().collect(Collectors.joining(",")));

            System.out.printf("[Scraper] scraped %s, found %d img src and %d neighbor urls, status=%d\n",
                    myUrl.toString(),
                    myImgSrcs.size(),
                    myAnchorHrefs.size(), myStatusCode);

            // `allMatch`: lazy streams stop iteration once `apply` fails
            myImgSrcs.stream().allMatch(aNotifier::notifyImgSrc);
            myAnchorHrefs.stream().allMatch(aNotifier::checkAndNotifyHref);
        } catch (IOException myException) {
            System.out.println(myException);
            return;
        } catch (IllegalArgumentException myException) {
            System.out.println(myException);
            return;
        }
    }

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
