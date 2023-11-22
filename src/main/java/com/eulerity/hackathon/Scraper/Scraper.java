package com.eulerity.hackathon.Scraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Scraper {
    private final URL theUrl;

    public Scraper(String aUrlString) throws MalformedURLException {
        theUrl = new URL(aUrlString);
        String myProtocol = theUrl.getProtocol();
        if (!myProtocol.equals("http") && !myProtocol.equals("https")) {
            throw new IllegalArgumentException("URL to scrape must use http/https protocol");
        }
    }

    public ScrapeResults scrape() {
        try {
            Document myDocument = Jsoup.connect(theUrl.toString()).get();

            Elements myAnchorElements = myDocument.getElementsByTag("a");
            List<String> myAnchorHrefs = myAnchorElements.stream()
                    .map((myElement) -> myElement.attr("href"))
                    .filter(myHref -> myHref != null && myHref.length() > 0)
                    .map(this::getFullUrl)
                    .collect(Collectors.toList());
            System.out.println(myAnchorHrefs.stream().collect(Collectors.joining(",")));

            Elements myImgElements = myDocument.getElementsByTag("img");
            System.out.println(myImgElements);
            List<String> myImgSrcs = myImgElements.stream()
                    .map((myElement) -> myElement.attr("src"))
                    .filter(mySrc -> mySrc != null && mySrc.length() > 0)
                    .map(this::getFullUrl)
                    .collect(Collectors.toList());
            System.out.println(myImgSrcs.stream().collect(Collectors.joining(",")));

            return new ScrapeResults(theUrl.toString(), myAnchorHrefs, myImgSrcs);
        } catch (IOException myException) {
            System.out.println(myException.getStackTrace());
            return new ScrapeResults(theUrl.toString());
        } catch (IllegalArgumentException myException) {
            System.out.println(myException.getStackTrace());
            return new ScrapeResults(theUrl.toString());
        }
    }

    private String getFullUrl(String aSrc) {
        if (aSrc.startsWith("https") || aSrc.startsWith("http")) {
            return aSrc;
        }
        if (aSrc.startsWith("//")) {
            return theUrl.getProtocol() + ':' + aSrc;
        }
        if (aSrc.startsWith("/")) {
            return theUrl.getProtocol() + "://" + theUrl.getHost() + aSrc;
        }
        return theUrl.getProtocol() + "://" + aSrc;
    }
}
