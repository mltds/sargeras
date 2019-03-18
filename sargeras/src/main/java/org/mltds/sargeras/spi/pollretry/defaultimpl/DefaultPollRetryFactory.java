package org.mltds.sargeras.spi.pollretry.defaultimpl;


import org.mltds.sargeras.spi.pollretry.PollRetry;
import org.mltds.sargeras.spi.pollretry.PollRetryFactory;

/**
 * @author sunyi.
 */
public class DefaultPollRetryFactory implements PollRetryFactory {

    private PollRetry pollRetry;

    @Override
    public PollRetry getObject() {

        if (pollRetry != null) {
            return pollRetry;
        }
        synchronized (DefaultPollRetryFactory.class) {
            if (pollRetry != null) {
                return pollRetry;
            }
            pollRetry = new DefaultPollRetry();
        }
        return pollRetry;
    }
}
