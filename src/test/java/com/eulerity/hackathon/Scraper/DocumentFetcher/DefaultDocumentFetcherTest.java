package com.eulerity.hackathon.Scraper.DocumentFetcher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.eulerity.hackathon.TestUtils;
import com.eulerity.hackathon.Scraper.RetryPolicy.RetryPolicy;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDocumentFetcherTest {
    @Mock
    private RetryPolicy theRetryPolicy;
    @Mock
    private ConnectionMaker theConnectionMaker;
    @Mock
    private Connection theConnection;
    @Mock
    private Connection.Response theResponse;
    @Mock
    Document theDocument;

    @Test
    public void shouldReturnNullForNonHttpProtocol() {
        for (String myInvalidUrl : TestUtils.INVALID_URLS) {
            assertNull(DefaultDocumentFetcher.INSTANCE.attemptToGetHtmlWithRetry(myInvalidUrl, theRetryPolicy,
                    TestUtils.TEST_WIDE_TIMEOUT_MS,
                    theConnectionMaker));
        }
    }

    @Test
    public void shouldRetryUntilLimitWithOnlyFailures() throws IOException {
        when(theConnectionMaker.makeConnection(eq(TestUtils.TEST_URL))).thenReturn(theConnection);
        when(theConnection.timeout(anyInt())).thenReturn(theConnection);
        when(theConnection.execute()).thenReturn(theResponse);
        when(theResponse.statusCode()).thenReturn(500);
        when(theResponse.parse()).thenReturn(theDocument);

        long myTotalRetries = 0;
        for (long myRetryLimit = 0; myRetryLimit < 39; myRetryLimit++) {
            final long myRetryLimitCopy = myRetryLimit;
            when(theRetryPolicy.getNextRetryMs()).thenAnswer(new Answer<Long>() {
                private long theRetries = 0;

                public Long answer(InvocationOnMock invocation) {
                    return (theRetries++ >= myRetryLimitCopy) ? RetryPolicy.DO_NOT_RETRY : 1L;
                }
            });

            assertNull(DefaultDocumentFetcher.INSTANCE.attemptToGetHtmlWithRetry(TestUtils.TEST_URL, theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS,
                            theConnectionMaker));
            myTotalRetries += (myRetryLimit + 1);
            verify(theConnectionMaker, times((int) myTotalRetries)).makeConnection(eq(TestUtils.TEST_URL));
        }
    }

    @Test
    public void shouldStopRetryAfterSuccess() throws IOException {
        when(theConnectionMaker.makeConnection(eq(TestUtils.TEST_URL))).thenReturn(theConnection);
        when(theConnection.timeout(anyInt())).thenReturn(theConnection);
        when(theConnection.execute()).thenReturn(theResponse);
        when(theResponse.parse()).thenReturn(theDocument);

        long myTotalRetries = 0;
        for (long myRetryLimit = 1; myRetryLimit < 39; myRetryLimit++) {
            final long myRetryLimitCopy = myRetryLimit;
            when(theRetryPolicy.getNextRetryMs()).thenAnswer(new Answer<Long>() {
                private long theRetries = 0;

                public Long answer(InvocationOnMock invocation) {
                    return (theRetries++ >= myRetryLimitCopy) ? RetryPolicy.DO_NOT_RETRY : 1L;
                }
            });

            final long mySuccessAfterRetries = (int) Math.random() * myRetryLimit;
            when(theResponse.statusCode()).thenAnswer(new Answer<Integer>() {
                private int theRetries = 0;

                public Integer answer(InvocationOnMock invocation) {
                    return (theRetries++ >= mySuccessAfterRetries) ? 200 : 500;
                }
            });

            assertNotNull(DefaultDocumentFetcher.INSTANCE.attemptToGetHtmlWithRetry(TestUtils.TEST_URL,
                theRetryPolicy, TestUtils.TEST_WIDE_TIMEOUT_MS, theConnectionMaker));
            myTotalRetries += (mySuccessAfterRetries + 1);
            verify(theConnectionMaker, times((int) myTotalRetries)).makeConnection(eq(TestUtils.TEST_URL));
        }
    }
}
