package org.mltds.sargeras.api;

import java.lang.reflect.Method;

/**
 * 用于构建一个 Saga
 *
 * @author sunyi
 */
public class SagaBuilder {

    private Saga saga;

    private SagaBuilder(Saga saga) {
        this.saga = saga;
    }

    public static SagaBuilder newBuilder() {
        Saga saga = new Saga();
        return new SagaBuilder(saga);
    }

    public SagaBuilder setAppName(String appName) {
        saga.setAppName(appName);
        return this;
    }

    public SagaBuilder setBizName(String bizName) {
        saga.setBizName(bizName);
        return this;
    }

    public SagaBuilder setBeanName(String beanName) {
        saga.setBeanName(beanName);
        return this;
    }

    public SagaBuilder setBeanClass(Class<?> beanClass) {
        saga.setBeanClass(beanClass);
        return this;
    }

    public SagaBuilder setMethod(Method method) {
        saga.setMethod(method);
        return this;
    }

    public SagaBuilder setParamTypes(Class[] methodParamTypes) {
        saga.setParameterTypes(methodParamTypes);
        return this;
    }

    public SagaBuilder setLockTimeout(int lockTimeout) {
        saga.setLockTimeout(lockTimeout);
        return this;
    }

    public SagaBuilder setBizTimeout(int bizTimeout) {
        saga.setBizTimeout(bizTimeout);
        return this;
    }

    /**
     * @param triggerInterval 例如{1,5,10,100} 第一次触发间隔为1秒，第二次5秒，第三次10秒，第四次及以后为100秒
     */
    public SagaBuilder setTriggerInterval(int[] triggerInterval) {
        saga.setTriggerInterval(triggerInterval);
        return this;
    }

    /**
     * @see #setTriggerInterval(int[])
     */
    public SagaBuilder setTriggerInterval(String triggerInterval) {
        saga.setTriggerInterval(triggerInterval);
        return this;
    }

    public Saga build() {
        return saga;
    }

}
