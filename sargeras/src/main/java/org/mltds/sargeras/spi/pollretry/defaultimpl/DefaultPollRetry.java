package org.mltds.sargeras.spi.pollretry.defaultimpl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.mltds.sargeras.api.SagaApplication;
import org.mltds.sargeras.api.SagaConfig;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.pollretry.PollRetry;
import org.mltds.sargeras.spi.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi.
 */
public class DefaultPollRetry implements PollRetry {

    public static final String POLLRETRY_PREFIX = "pollretry.default.";
    /**
     * 重试线程数
     */
    public static final String POLLRETRY_NTHREADS = POLLRETRY_PREFIX + "nthreads";
    /**
     * 每批获取的需要补偿的条数
     */
    public static final String POLLRETRY_LIMIT = POLLRETRY_PREFIX + "limit";
    /**
     * 批次之间的时间间隔
     */
    public static final String POLLRETRY_INTERVAL = POLLRETRY_PREFIX + "interval";

    private static final Logger logger = LoggerFactory.getLogger(DefaultPollRetry.class);

    @Override
    public void startPollRetry() {

        int nThreads = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_NTHREADS, "1"));

        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.submit(new PollRetryThread(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdownNow();
            }
        });

    }

    private static class PollRetryThread implements Runnable {
        @Override
        public void run() {
            while (true) {

                Manager manager = SagaApplication.getManager();
                Service service = SagaApplication.getService();
                int limit = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_LIMIT, "100"));
                int interval = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_INTERVAL, "1"));

                List<Long> needRetryContextList = manager.findNeedRetryContextList(new Date(), limit);

                for (Long contextId : needRetryContextList) {
                    try {
                        service.retry(contextId);
                    } catch (Exception e) {
                        logger.warn("轮询重试期间发生异常, Context ID:" + contextId, e);
                    }
                }

                try {
                    Thread.sleep(interval * 1000);// 转换成毫秒
                } catch (Exception e) {
                }
            }
        }
    }

}
