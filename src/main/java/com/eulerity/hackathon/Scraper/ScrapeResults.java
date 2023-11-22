package com.eulerity.hackathon.Scraper;

import java.util.ArrayList;
import java.util.List;

public class ScrapeResults {
    private final String theUrl;
    private final List<String> theNeighborUrls;
    private final List<String> theImageUrls;

    public ScrapeResults(String aUrl) {
        theUrl = aUrl;
        theNeighborUrls = new ArrayList<>();
        theImageUrls = new ArrayList<>();
    }

    public ScrapeResults(String aUrl, List<String> aNeighborUrls, List<String> aImageUrls) {
        theUrl = aUrl;
        theNeighborUrls = aNeighborUrls;
        theImageUrls = aImageUrls;
    }

    public String getUrl() {
        return theUrl;
    }

    public List<String> getNeighborUrls() {
        return theNeighborUrls;
    }

    public List<String> getImageUrls() {
        return theImageUrls;
    }

    public void addNeighborUrl(String aNeighborUrl) {
        theNeighborUrls.add(aNeighborUrl);
    }

    public void addImageUrl(String aImageUrl) {
        theImageUrls.add(aImageUrl);
    }
}
