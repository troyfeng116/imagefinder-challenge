package com.eulerity.hackathon.Scraper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.eulerity.hackathon.TestUtils;
import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.Notifiers.CrawlerNotifier;
import com.eulerity.hackathon.Scraper.DocumentFetcher.DocumentFetcher;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

@RunWith(MockitoJUnitRunner.class)
public class ScraperTest {
    private static final String[] IMG_SRCS = {
            "https://domain.com/img0.webp",
            TestUtils.TEST_URL + "/img1.webp",
            "img2.webp",
            "img3.jpg",
            "/assets/img4.jpg" };

    private static final String[] HREFS = {
            TestUtils.TEST_URL,
            TestUtils.TEST_URL + "/",
            TestUtils.TEST_URL + "/path1",
            TestUtils.TEST_URL + "/path/path2",
            "path",
            "/path"
    };

    private Scraper theScraper;
    private CrawlerConfig theConfig;
    private static URL theUrl;

    @Mock
    private DocumentFetcher theFetcher;
    @Mock
    private CrawlerNotifier theCrawlerNotifier;
    @Mock
    private RetryPolicy theRetryPolicy;
    @Mock
    private Document theDocument;
    @Mock
    private Elements theImgElements;
    @Mock
    private Elements theAnchorElements;

    @Before
    public void setUp() throws MalformedURLException {
        theScraper = new Scraper();
        theUrl = new URL(TestUtils.TEST_URL);
        theConfig = new CrawlerConfig.Builder(theUrl).build();
    }

    @Test
    public void shouldReturnFalseOnInvalidUrl() throws MalformedURLException {
        for (String myInvalidUrl : TestUtils.INVALID_URLS) {
            assertFalse(theScraper.scrape(myInvalidUrl, theConfig, theCrawlerNotifier, theRetryPolicy,
                    TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        }

        verifyZeroInteractions(theCrawlerNotifier);
    }

    @Test
    public void shouldReturnFalseOnFetcherFailure() {
        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(null);

        assertFalse(theScraper.scrape(TestUtils.TEST_URL, theConfig, theCrawlerNotifier, theRetryPolicy,
                TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        verifyZeroInteractions(theCrawlerNotifier);
    }

    @Test
    public void shouldReturnTrueWithoutNotifyForNoImgsOrUrls() {
        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(theDocument);
        when(theDocument.getElementsByTag(anyString())).thenReturn(theImgElements).thenReturn(theAnchorElements);
        when(theImgElements.stream()).thenReturn(Stream.of());
        when(theAnchorElements.stream()).thenReturn(Stream.of());

        assertTrue(theScraper.scrape(TestUtils.TEST_URL, theConfig, theCrawlerNotifier, theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        verifyZeroInteractions(theCrawlerNotifier);
    }

    @Test
    public void shouldReturnTrueAndNotifyAllImgs() {
        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(theDocument);
        when(theDocument.getElementsByTag(eq("img"))).thenReturn(theImgElements);
        when(theImgElements.stream()).thenReturn(toImgElements(IMG_SRCS).stream());
        when(theDocument.getElementsByTag(eq("a"))).thenReturn(theAnchorElements);
        when(theAnchorElements.stream()).thenReturn(Stream.of());
        when(theCrawlerNotifier.notifyImgSrc(anyString())).thenReturn(true);

        assertTrue(theScraper.scrape(TestUtils.TEST_URL, theConfig, theCrawlerNotifier, theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        verify(theCrawlerNotifier, times(IMG_SRCS.length)).notifyImgSrc(anyString());
    }

    @Test
    public void shouldStopNotifyingImgSrcOptimisticallyOnNotifierReturnFalse() {
        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(theDocument);
        when(theDocument.getElementsByTag(eq("img"))).thenReturn(theImgElements);
        when(theImgElements.stream()).thenReturn(toImgElements(IMG_SRCS).stream());
        when(theDocument.getElementsByTag(eq("a"))).thenReturn(theAnchorElements);
        when(theAnchorElements.stream()).thenReturn(Stream.of());
        when(theCrawlerNotifier.notifyImgSrc(anyString())).thenReturn(true).thenReturn(true).thenReturn(false);

        assertTrue(theScraper.scrape(TestUtils.TEST_URL, theConfig, theCrawlerNotifier, theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        verify(theCrawlerNotifier, times(3)).notifyImgSrc(anyString());
    }

    @Test
    public void shouldReturnTrueAndNotifyAllHrefs() {
        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(theDocument);
        when(theDocument.getElementsByTag(eq("img"))).thenReturn(theImgElements);
        when(theImgElements.stream()).thenReturn(Stream.of());
        when(theDocument.getElementsByTag(eq("a"))).thenReturn(theAnchorElements);
        when(theAnchorElements.stream()).thenReturn(toAnchorElements(HREFS).stream());
        when(theCrawlerNotifier.checkAndNotifyHref(anyString())).thenReturn(true);

        assertTrue(theScraper.scrape(TestUtils.TEST_URL, theConfig, theCrawlerNotifier, theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        verify(theCrawlerNotifier, times(HREFS.length)).checkAndNotifyHref(anyString());
    }

    @Test
    public void shouldStopNotifyingHrefOptimisticallyOnNotifierReturnFalse() {
        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(theDocument);
        when(theDocument.getElementsByTag(eq("img"))).thenReturn(theImgElements);
        when(theImgElements.stream()).thenReturn(Stream.of());
        when(theDocument.getElementsByTag(eq("a"))).thenReturn(theAnchorElements);
        when(theAnchorElements.stream()).thenReturn(toAnchorElements(HREFS).stream());
        when(theCrawlerNotifier.checkAndNotifyHref(anyString())).thenReturn(true).thenReturn(true).thenReturn(false);

        assertTrue(theScraper.scrape(TestUtils.TEST_URL, theConfig, theCrawlerNotifier, theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        verify(theCrawlerNotifier, times(3)).checkAndNotifyHref(anyString());
    }

    @Test
    public void shouldNotifyAllImgSrcsAndHrefs() {
        when(theFetcher.attemptToGetHtmlWithRetry(any(), eq(theRetryPolicy), anyLong(), any())).thenReturn(theDocument);
        when(theDocument.getElementsByTag(eq("img"))).thenReturn(theImgElements);
        when(theImgElements.stream()).thenReturn(toImgElements(IMG_SRCS).stream());
        when(theDocument.getElementsByTag(eq("a"))).thenReturn(theAnchorElements);
        when(theAnchorElements.stream()).thenReturn(toAnchorElements(HREFS).stream());
        when(theCrawlerNotifier.notifyImgSrc(anyString())).thenReturn(true);
        when(theCrawlerNotifier.checkAndNotifyHref(anyString())).thenReturn(true);

        assertTrue(theScraper.scrape(TestUtils.TEST_URL, theConfig, theCrawlerNotifier, theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS, theFetcher));
        verify(theCrawlerNotifier, times(IMG_SRCS.length)).notifyImgSrc(anyString());
        verify(theCrawlerNotifier, times(HREFS.length)).checkAndNotifyHref(anyString());
    }

    private static List<Element> toImgElements(String[] aSrcs) {
        List<Element> myImgElements = new ArrayList<>();
        for (String mySrc : aSrcs) {
            Element myElement = new Element("img");
            myElement.attr("src", ScraperUtils.getFullUrl(theUrl, mySrc));
            myImgElements.add(myElement);
        }
        return myImgElements;
    }

    private static List<Element> toAnchorElements(String[] aHrefs) {
        List<Element> myAnchorElements = new ArrayList<>();
        for (String myHref : aHrefs) {
            Element myElement = new Element("a");
            myElement.attr("href", ScraperUtils.getFullUrl(theUrl, myHref));
            myAnchorElements.add(myElement);
        }
        return myAnchorElements;
    }
}
