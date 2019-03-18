package org.mltds.sargeras.spi.pollretry;

import org.mltds.sargeras.spi.SagaBean;

/**
 * @author sunyi.
 */
public interface PollRetry extends SagaBean {

    void startPollRetry();

}
