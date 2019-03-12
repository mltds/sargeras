package org.mltds.sargeras.api;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.mltds.sargeras.exception.SagaException;
import org.mltds.sargeras.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi
 */
public class SagaLauncher {

    private static final Logger logger = LoggerFactory.getLogger(SagaLauncher.class);

    private static final String CONFIG_FACTORY_PREFIX = "factory.";

    public static void launch() {

        logger.info("Saga Launch 开始启动...");
        initBeanFactory(); // 初始化 BeanFactory

        // 注册 Saga

        // 启动轮询重试线程

        logger.info("Saga Launch 启动完成...");

    }

    @SuppressWarnings("unchecked")
    private static void initBeanFactory() {
        try {
            Set<Map.Entry<Object, Object>> entries = SagaConfig.getAllProperties().entrySet();
            for (Map.Entry e : entries) {

                String key = e.getKey().toString();

                if (!key.startsWith(CONFIG_FACTORY_PREFIX)) {
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

}
