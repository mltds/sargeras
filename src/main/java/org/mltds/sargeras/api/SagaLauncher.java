package org.mltds.sargeras.api;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mltds.sargeras.api.spi.SagaBean;
import org.mltds.sargeras.api.spi.SagaBeanFactory;
import org.mltds.sargeras.exception.SagaException;
import org.mltds.sargeras.manager.Manager;
import org.mltds.sargeras.repository.Repository;
import org.mltds.sargeras.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi
 */
public class SagaLauncher {

    private static final Logger logger = LoggerFactory.getLogger(SagaLauncher.class);

    private static final AtomicBoolean launched = new AtomicBoolean(false);

    public static void launch() {

        if (launched.get()) {
            return;
        }

        synchronized (SagaLauncher.class) {
            if (launched.get()) {
                return;
            }

            logger.info("Saga Launch 开始启动...");

            // 初始化 BeanFactory
            initBeanFactory();

            // 启动轮询重试线程
            initPollRetryThread();

            logger.info("Saga Launch 启动完成...");

            launched.set(true);
        }

    }

    @SuppressWarnings("unchecked")
    private static void initBeanFactory() {
        try {
            Set<Map.Entry<Object, Object>> entries = SagaConfig.getAllProperties().entrySet();
            for (Map.Entry e : entries) {

                String key = e.getKey().toString();

                if (!key.startsWith(SagaConfig.FACTORY_PREFIX)) {
                    continue;
                }

                String value = e.getValue().toString();
                Class factoryClass = Utils.loadClass(value);
                if (factoryClass == null) {
                    continue;
                }

                boolean isFactoryClass = SagaBeanFactory.class.isAssignableFrom(factoryClass);
                if (!isFactoryClass) {
                    continue;
                }

                SagaBeanFactory beanFactory = (SagaBeanFactory) factoryClass.newInstance(); // 实例化BeanFactory
                Method getObject = factoryClass.getMethod("getObject");
                Class<? extends SagaBean> bean = (Class<? extends SagaBean>) getObject.getReturnType();
                SagaApplication.addBeanFactory(bean, beanFactory);// 根据 Bean 的类型，缓存 BeanFactory
                if (logger.isDebugEnabled()) {
                    logger.debug("初始化" + bean.getSimpleName() + "BeanFactory成功: " + factoryClass.getName());
                }
            }
        } catch (Throwable e) {
            throw new SagaException("Saga 初始化 BeanFactory 失败!!!", e);
        }
        logger.info("Saga 初始化 BeanFactory 成功...");
    }

    public static void initPollRetryThread() {

        int nThreads = Integer.valueOf(SagaConfig.getProperty(SagaConfig.POLLRETRY_NTHREADS));

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

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

    public static class PollRetryThread implements Runnable {
        @Override
        public void run() {
            while (true) {

                Repository repository = SagaApplication.getRepository();
                Manager manager = SagaApplication.getManager();
                int limit = Integer.valueOf(SagaConfig.getProperty(SagaConfig.POLLRETRY_LIMIT));
                int interval = Integer.valueOf(SagaConfig.getProperty(SagaConfig.POLLRETRY_INTERVAL));

                List<Long> needRetryContextList = repository.findNeedRetryContextList(limit);

                for (Long contextId : needRetryContextList) {
                    try {
                        manager.pollRetry(contextId);
                    } catch (Exception e) {
                        logger.warn("轮询重试期间发生异常, Context ID:" + contextId, e);
                    }
                }

                try {
                    Thread.sleep(interval * 1000);// 转换成秒
                } catch (InterruptedException e) {
                }
            }
        }
    }

}
