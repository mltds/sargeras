package org.mltds.sargeras.api;

import org.mltds.sargeras.api.listener.SagaListener;

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

    public static SagaBuilder newBuilder(String appName, String bizName) {
        Saga saga = new Saga(appName, bizName);
        return new SagaBuilder(saga);
    }

    public SagaBuilder addTx(SagaTx tx) {
        saga.addTx(tx);
        return this;
    }

    public SagaBuilder addListener(SagaListener listener) {
        saga.addListener(listener);
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
     * @return
     */
    public SagaBuilder setTriggerInterval(int[] triggerInterval) {
        saga.setTriggerInterval(triggerInterval);
        return this;
    }

    public Saga build() {
        SagaApplication.addSaga(saga);// cache saga
        return saga;
    }

}
