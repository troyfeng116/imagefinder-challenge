package com.eulerity.hackathon.Scraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;

public class Scraper {
    private final URL theUrl;

    public Scraper(String aUrlString) throws MalformedURLException {
        theUrl = new URL(aUrlString);
        String myProtocol = theUrl.getProtocol();
        if (!myProtocol.equals("http") && !myProtocol.equals("https")) {
            throw new IllegalArgumentException("URL to scrape must use http/https protocol");
        }
    }

    public static void scrape(String aUrl,
            boolean aShouldIncludeSVGs,
            boolean aShouldIncludePNGs,
            CrawlerNotifier aNotifier) {
        try {
            URL myUrl = new URL(aUrl);
            String myDomain = myUrl.getHost();
            Document myDocument = Jsoup.connect(aUrl).get();

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

            System.out.printf("scraped %s, found %d img src and %d neighbor urls\n", myUrl.toString(), myImgSrcs.size(),
                    myAnchorHrefs.size());

            // `allMatch`: lazy streams stop iteration once `apply` fails
            myImgSrcs.stream().allMatch(aNotifier::notifyImgSrc);
            myAnchorHrefs.stream().allMatch(aNotifier::notifyHref);
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
