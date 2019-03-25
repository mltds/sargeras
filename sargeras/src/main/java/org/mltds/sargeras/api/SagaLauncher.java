//package org.mltds.sargeras.api;
//
//import java.lang.reflect.Method;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//import java.util.TreeSet;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import org.mltds.sargeras.api.exception.SagaException;
//import org.mltds.sargeras.spi.SagaBean;
//import org.mltds.sargeras.spi.pollretry.PollRetry;
//import org.mltds.sargeras.utils.Utils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * @author sunyi
// */
//public class SagaLauncher {
//
//    private static final Logger logger = LoggerFactory.getLogger(SagaLauncher.class);
//
//    private static final AtomicBoolean launched = new AtomicBoolean(false);
//
//    public static void launch() {
//        launch(null);
//    }
//
//    public static void launch(Properties properties) {
//
//        if (launched.get()) {
//            return;
//        }
//
//        synchronized (SagaLauncher.class) {
//            if (launched.get()) {
//                return;
//            }
//
//            logger.info("Saga Launch 开始启动...");
//
//            // 加载配置
//            initConfig(properties);
//
//            // 初始化 SPI BeanFactory
//            initBeanFactory();
//
//            // 启动轮询重试
//            startPollRetry();
//
//            launched.set(true);
//
//            logger.info("Saga Launch 启动完成...");
//        }
//
//    }
//
//    private static void initConfig(Properties properties) {
//        // 加载配置
//        SagaConfig.load(properties);
//
//        if (logger.isDebugEnabled()) {
//            Properties allProperties = SagaConfig.getAllProperties();
//            logger.debug("Saga配置信息:");
//            Set<Object> set = new TreeSet<>();
//            set.addAll(allProperties.keySet());
//            for (Object k : set) {
//                String key = k.toString();
//                logger.debug(key + "=" + allProperties.getProperty(key));
//            }
//        }
//
//    }
//
//    @SuppressWarnings("unchecked")
//    private static void initBeanFactory() {
//        try {
//            Set<Map.Entry<Object, Object>> entries = SagaConfig.getAllProperties().entrySet();
//            for (Map.Entry e : entries) {
//
//                String key = e.getKey().toString();
//
//                if (!key.startsWith(SagaConfig.SPI_FACTORY_PREFIX)) {
//                    continue;
//                }
//
//                String value = e.getValue().toString();
//                Class factoryClass = Utils.loadClass(value);
//                if (factoryClass == null) {
//                    continue;
//                }
//
//                boolean isFactoryClass = SagaBeanFactory.class.isAssignableFrom(factoryClass);
//                if (!isFactoryClass) {
//                    continue;
//                }
//
//                SagaBeanFactory beanFactory = (SagaBeanFactory) factoryClass.newInstance(); // 实例化BeanFactory
//                Method getObject = factoryClass.getMethod("getObject");
//                Class<? extends SagaBean> bean = (Class<? extends SagaBean>) getObject.getReturnType();
//                SagaApplication.addBeanFactory(bean, beanFactory);// 根据 Bean 的类型，缓存 BeanFactory
//                if (logger.isDebugEnabled()) {
//                    logger.debug("初始化" + bean.getSimpleName() + "BeanFactory成功: " + factoryClass.getName());
//                }
//            }
//        } catch (Throwable e) {
//            throw new SagaException("Saga 初始化 BeanFactory 失败!!!", e);
//        }
//        logger.debug("Saga 初始化 BeanFactory 成功...");
//    }
//
//    private static void startPollRetry() {
//        PollRetry pollRetry = SagaApplication.getPollRetry();
//        pollRetry.startPollRetry();
//        logger.debug("Saga 轮询重试模块启动成功...");
//    }
//
//}
