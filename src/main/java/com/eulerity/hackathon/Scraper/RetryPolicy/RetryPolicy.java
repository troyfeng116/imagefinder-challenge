package com.eulerity.hackathon.Scraper.RetryPolicy;

/**
 * @NotThreadSafe
 */
public abstract class RetryPolicy {
    public static long DO_NOT_RETRY = -1L;

    private final int theMaxRetries;
    private int theRetries;

    public RetryPolicy(int aMaxRetries) {
        theMaxRetries = aMaxRetries;
        theRetries = 0;
    }

    public int getMaxRetries() {
        return theMaxRetries;
    }

    public long getNextRetryMs() {
        if (theRetries >= theMaxRetries) {
            return DO_NOT_RETRY;
        }

        theRetries++;
        return getNextRetryDelayMs();
    }

    protected abstract long getNextRetryDelayMs();
}
