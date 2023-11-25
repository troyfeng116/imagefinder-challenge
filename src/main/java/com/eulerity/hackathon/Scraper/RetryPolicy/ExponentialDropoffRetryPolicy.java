package com.eulerity.hackathon.Scraper.RetryPolicy;

public class ExponentialDropoffRetryPolicy extends RetryPolicy {
    private final double theMultiplier;
    private long theCurrentRetryDelayMs;

    public ExponentialDropoffRetryPolicy(int aMaxRetries, long aStartRetryDelayMs, double aMultiplier) {
        super(aMaxRetries);
        theMultiplier = aMultiplier;
        theCurrentRetryDelayMs = aStartRetryDelayMs;
    }

    @Override
    protected long getNextRetryDelayMs() {
        long myDelay = theCurrentRetryDelayMs;
        theCurrentRetryDelayMs *= theMultiplier;
        return myDelay;
    }
}
