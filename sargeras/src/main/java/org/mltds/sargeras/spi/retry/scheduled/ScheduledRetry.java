package org.mltds.sargeras.spi.retry.scheduled;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaTxControl;
import org.mltds.sargeras.api.model.SagaRecord;
import org.mltds.sargeras.api.model.SagaRecordParam;
import org.mltds.sargeras.core.SagaApplication;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.retry.Retry;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;

/**
 * @author sunyi.
 */
@Component
public class ScheduledRetry implements Retry, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledRetry.class);
    /**
     * 重试线程数
     */
    @Value("${pollretry.scheduled.nthreads:1}")
    private int nThreads;
    /**
     * 每批获取的需要重试的条数
     */
    @Value("${pollretry.scheduled.limit:100}")
    private int limit;
    /**
     * 轮询的时间间隔
     */
    @Value("${pollretry.scheduled.interval:1}")
    private int interval;

    private volatile boolean running = false;

    @Autowired
    private Manager manager;

    @Autowired
    private Serializer serializer;

    @Autowired
    private SagaApplication sagaApplication;

    private ScheduledThreadPoolExecutor executorService;

    private DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private ApplicationContext applicationContext;

    @Override
    @PostConstruct
    public void init() {

        running = true;

        executorService = new ScheduledThreadPoolExecutor(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.scheduleAtFixedRate(new RetryThread(), 0, interval, TimeUnit.SECONDS);
        }

    }

    @Override
    @PreDestroy
    public void destroy() {

        running = false;

        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private class RetryThread implements Runnable {
        @Override
        public void run() {

            try {
                List<Long> needRetryRecordListList = manager.findNeedRetryRecordList(new Date(), limit);

                for (Long recordId : needRetryRecordListList) {
                    try {

                        if (!running) {
                            return;
                        }

                        SagaRecord record = manager.findRecord(recordId);
                        if (record.isLocked() && record.getLockExpireTime().after(new Date())) {
                            continue;
                        }

                        List<SagaRecordParam> recordParamList = manager.findRecordParam(recordId);

                        Map<String, SagaRecordParam> recordParamMap = new HashMap<>();
                        for (SagaRecordParam recordParam : recordParamList) {
                            recordParamMap.put(recordParam.getParameterName(), recordParam);
                        }

                        Saga saga = sagaApplication.getSaga(record.getAppName(), record.getBizName());
                        Method method = saga.getMethod();
                        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
                        Object[] args = new Object[parameterNames.length];

                        for (int i = 0; i < parameterNames.length; i++) {
                            String parameterName = parameterNames[i];
                            SagaRecordParam recordParam = recordParamMap.get(parameterName);
                            if (recordParam != null) {
                                Object arg = serializer.decode(recordParam.getParameter(), Utils.loadClass(recordParam.getParameterType()));
                                args[i] = arg;
                            }
                        }

                        String beanName = saga.getBeanName();
                        Class<?> cls = saga.getBeanClass();
                        Object bean = applicationContext.getBean(beanName, cls);

                        method.invoke(bean, args);

                    } catch (Exception e) {
                        if (!(e instanceof SagaTxControl)) {
                            logger.warn("轮询重试期间发生异常, Record ID:" + recordId,
                                    (e instanceof InvocationTargetException) ? ((InvocationTargetException) e).getTargetException() : e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("轮询重试期间发生异常", e);
            }
        }
    }

}
