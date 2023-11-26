package com.eulerity.hackathon.Scraper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.eulerity.hackathon.TestUtils;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Scraper.DocumentFetcher.DocumentFetcher;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

@RunWith(MockitoJUnitRunner.class)
public class ScraperTest {
    private Scraper theScraper;

    @Mock
    private DocumentFetcher theFetcher;
    @Mock
    private CrawlerNotifier theCrawlerNotifier;
    @Mock
    private RetryPolicy theRetryPolicy;
    @Mock
    private Document theDocument;

    @Before
    public void setUp() {
        theScraper = new Scraper();
    }

    @Test
    public void shouldReturnFalseOnInvalidUrl() throws MalformedURLException {
        final CrawlerConfig myConfig = new CrawlerConfig.Builder(new URL("https://google.com")).build();

        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(null);

        for (String myInvalidUrl : TestUtils.INVALID_URLS) {
            assertFalse(theScraper.scrape(myInvalidUrl, myConfig, theCrawlerNotifier, theRetryPolicy,
                    TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        }

        verifyZeroInteractions(theCrawlerNotifier);
    }
}
