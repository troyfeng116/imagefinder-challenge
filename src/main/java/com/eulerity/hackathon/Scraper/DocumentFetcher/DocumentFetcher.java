package com.eulerity.hackathon.Scraper.DocumentFetcher;

import org.jsoup.nodes.Document;

import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

public interface DocumentFetcher {

    /**
     * Attempts to get HTML document for jsoup scraping via HTTP GET. Retries
     * according to `aRetryPolicy`.
     * 
     * @param aUrl         HTTP/HTTPS URL for jsoup connection.
     * @param aRetryPolicy Retry policy incase of retry-able http status codes.
     * @param aTimeLimitMs Connection timeout limit for request.
     * @return `Document` object if 200 status code response, `null` if no
     *         successful tries.
     */
    Document attemptToGetHtmlWithRetry(String aUrl, RetryPolicy aRetryPolicy, long aTimeLimitMs,
            ConnectionMaker aConnectionMaker);
}
