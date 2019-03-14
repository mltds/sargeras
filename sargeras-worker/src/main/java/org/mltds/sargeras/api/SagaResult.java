package org.mltds.sargeras.api;

/**
 * @author sunyi.
 */
public class SagaResult {

    private Saga saga;
    private SagaStatus status;
    private SagaContext context;

    public Saga getSaga() {
        return saga;
    }

    public void setSaga(Saga saga) {
        this.saga = saga;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public SagaContext getContext() {
        return context;
    }

    public void setContext(SagaContext context) {
        this.context = context;
    }

    public String getBizId() {
        return context.getBizId();
    }

    public <T> T getBizParam(Class<T> tClass) {
        return context.getBizParam(tClass);
    }

    public <T> T getBizResult(Class<T> tClass) {
        return context.getBizResult(tClass);
    }
}
