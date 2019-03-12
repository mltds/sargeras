package org.mltds.sargeras.api;

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

    public Saga build() {
        SagaApplication.addSaga(saga);// cache saga
        return saga;
    }

}
