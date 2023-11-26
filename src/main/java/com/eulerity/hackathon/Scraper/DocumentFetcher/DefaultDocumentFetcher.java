package com.eulerity.hackathon.Scraper.DocumentFetcher;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eulerity.hackathon.Crawler.CrawlerUtils;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

public enum DefaultDocumentFetcher implements DocumentFetcher {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDocumentFetcher.class);

    private DefaultDocumentFetcher() {
    }

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
    @Override
    public Document attemptToGetHtmlWithRetry(String aUrl, RetryPolicy aRetryPolicy, long aTimeLimitMs,
            ConnectionMaker aConnectionMaker) {
        long myStartTimestampMs = System.currentTimeMillis();
        Document myDocument = null;
        while (myDocument == null) {
            try {
                Connection myConnection = aConnectionMaker.makeConnection(aUrl);
                int myTimeRemainingMs = Math.max(1,
                        (int) (aTimeLimitMs - CrawlerUtils.getTimeElapsedSinceMs(myStartTimestampMs)));
                Connection.Response myResponse = myConnection
                        .timeout(myTimeRemainingMs)
                        .execute();
                myDocument = myResponse.parse();
                int myStatusCode = myResponse.statusCode();
                if (myStatusCode == 200) {
                    break;
                }

                if (myStatusCode == 500 || myStatusCode == 502 || myStatusCode == 503 || myStatusCode == 504) {
                    long myRetryDelayMs = aRetryPolicy.getNextRetryMs();
                    if (myRetryDelayMs == RetryPolicy.DO_NOT_RETRY) {
                        LOGGER.debug(String.format("no longer retrying %s for status %d", aUrl, myStatusCode));
                        return null;
                    }

                    // retry
                    LOGGER.debug(String.format("retrying %s for status %d after %d ms sleep", aUrl, myStatusCode,
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
}
