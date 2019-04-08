package org.mltds.sargeras.core;

import java.lang.reflect.Method;

import org.mltds.sargeras.api.SagaBuilder;
import org.mltds.sargeras.api.annotation.Saga;
import org.mltds.sargeras.api.model.MethodInfo;
import org.mltds.sargeras.core.aop.SagaAopComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.stereotype.Component;

/**
 * @author sunyi.
 */
@Component
public class SagaScan extends InstantiationAwareBeanPostProcessorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SagaScan.class);

    @Autowired
    private SagaAopComponent aopComponent;

    @Autowired
    private SagaApplication sagaApplication;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Class<?> beanClass = bean.getClass();

        Method[] methods = beanClass.getMethods();

        for (Method method : methods) {
            Saga anno = method.getAnnotation(Saga.class);
            if (anno != null) {
                registrySaga(beanName, method, anno);
            }
        }

        return super.postProcessBeforeInitialization(bean, beanName);
    }

    private void registrySaga(String beanName, Method method, Saga anno) {

        String appName = anno.appName();
        String bizName = anno.bizName();

        org.mltds.sargeras.api.Saga saga = sagaApplication.getSaga(appName, bizName);
        if (saga != null) {
            return;
        }

        MethodInfo methodInfo = aopComponent.getMethodInfo(method);
        saga = buildSaga(beanName, anno, methodInfo);
        sagaApplication.addSaga(saga);

        logger.info("注册Saga：" + saga.getKeyName());
    }

    private org.mltds.sargeras.api.Saga buildSaga(String beanName, org.mltds.sargeras.api.annotation.Saga anno, MethodInfo methodInfo) {

        String appName = anno.appName();
        String bizName = anno.bizName();
        int bizTimeout = anno.bizTimeout();
        int lockTimeout = anno.lockTimeout();
        String triggerInterval = anno.triggerInterval();

        SagaBuilder builder = SagaBuilder.newBuilder();
        builder.setAppName(appName)//
                .setBizName(bizName)//
                .setBeanClass(methodInfo.getCls())//
                .setBeanName(beanName)//
                .setMethod(methodInfo.getMethod())//
                .setParamTypes(methodInfo.getParameterTypes())//
                .setBizTimeout(bizTimeout)//
                .setLockTimeout(lockTimeout)//
                .setTriggerInterval(triggerInterval)//
        ;

        return builder.build();
    }

}
