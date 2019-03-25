package org.mltds.sargeras.spi.pollretry.defaultimpl;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaConfig;
import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.model.SagaRecordParam;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.pollretry.PollRetry;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;

/**
 * @author sunyi.
 */
public class DefaultPollRetry implements PollRetry, ApplicationContextAware {

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

    @Autowired
    private Manager manager;

    @Autowired
    private Serializer serializer;

    private ApplicationContext applicationContext;

    private DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private class PollRetryThread implements Runnable {
        @Override
        public void run() {

            int limit = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_LIMIT, "100"));
            int interval = Integer.valueOf(SagaConfig.getProperty(POLLRETRY_INTERVAL, "1"));

            while (true) {
                try {

                    List<Long> needRetryRecordListList = manager.findNeedRetryRecordList(new Date(), limit);

                    for (Long recordId : needRetryRecordListList) {
                        try {

                            SagaContext context = SagaContext.loadContext(recordId);

                            List<SagaRecordParam> recordParamList = context.getRecordParam(recordId);

                            Map<String, SagaRecordParam> recordParamMap = new HashMap<>();
                            for (SagaRecordParam recordParam : recordParamList) {
                                recordParamMap.put(recordParam.getParameterName(), recordParam);
                            }

                            Saga saga = context.getSaga();
                            Method method = saga.getMethod();
                            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
                            Object[] args = new Object[] { parameterNames.length };

                            for (int i = 0; i < parameterNames.length; i++) {
                                String parameterName = parameterNames[i];
                                SagaRecordParam recordParam = recordParamMap.get(parameterName);
                                if (recordParam != null) {
                                    Object arg = serializer.deserialize(recordParam.getParameter(), Utils.loadClass(recordParam.getParameterType()));
                                    args[i] = arg;
                                }
                            }

                            Object bean = saga.getBean();

                            method.invoke(bean, args);

                        } catch (Exception e) {
                            logger.warn("轮询重试期间发生异常, Record ID:" + recordId, e);
                        }
                    }

                    Thread.sleep(interval * 1000);// 转换成毫秒
                } catch (Exception e) {
                }
            }
        }
    }

}
