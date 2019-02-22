package org.mltds.sargeras;

/**
 * 用于构建一个 Saga
 * 
 * @author sunyi 2019/2/15.
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
        //TODO store saga to memory
        return saga;
    }

}
