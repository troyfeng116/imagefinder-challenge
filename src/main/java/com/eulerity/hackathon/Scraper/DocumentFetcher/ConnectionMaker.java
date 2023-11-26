package com.eulerity.hackathon.Scraper.DocumentFetcher;

import org.jsoup.Connection;

@FunctionalInterface
public interface ConnectionMaker {
    Connection makeConnection(String aUrl);
}
