package org.mltds.sargeras.worker.spi.pollretry.defaultimpl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.mltds.sargeras.server.facade.ServerFacade;
import org.mltds.sargeras.worker.api.SagaApplication;
import org.mltds.sargeras.worker.api.SagaConfig;
import org.mltds.sargeras.worker.spi.manager.Manager;
import org.mltds.sargeras.worker.spi.pollretry.PollRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi.
 */
public class DefaultPollRetry implements PollRetry {

    public static final String POLLRETRY_PREFIX = "pollretry.default.";
    public static final String POLLRETRY_NTHREADS = POLLRETRY_PREFIX + "nthreads";
    public static final String POLLRETRY_LIMIT = POLLRETRY_PREFIX + "limit";
    public static final String POLLRETRY_INTERVAL = POLLRETRY_PREFIX + "interval";

    private static final Logger logger = LoggerFactory.getLogger(DefaultPollRetry.class);

    @Override
    public void startPollRetry() {

        int nThreads = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_NTHREADS));

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

                ServerFacade serverFacade = SagaApplication.getServerFacade();
                Manager manager = SagaApplication.getManager();
                int limit = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_LIMIT));
                int interval = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_INTERVAL));

                List<Long> needRetryContextList = serverFacade.findNeedRetryContextList(limit);

                for (Long contextId : needRetryContextList) {
                    try {
                        manager.retry(contextId);
                    } catch (Exception e) {
                        logger.warn("轮询重试期间发生异常, Context ID:" + contextId, e);
                    }
                }

                try {
                    Thread.sleep(interval * 1000);// 转换成秒
                } catch (Exception e) {
                }
            }
        }
    }

}
