package org.mltds.sargeras.spi.pollretry;

/**
 * @author sunyi.
 */
public interface PollRetry {

    /**
     * 启动轮询重试，组件自己执行，外部不调用
     */
    void init();

    /**
     * 终止轮询重试，组件自己执行，外部不调用
     */
    void destroy();

}
