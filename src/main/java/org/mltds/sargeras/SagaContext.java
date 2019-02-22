package org.mltds.sargeras;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sunyi 2019/2/15.
 */
public class SagaContext {

    private Saga saga;

    private Long id;
    private String bizId;
    private Object bizParam;
    private Object bizResult;

    private SagaStatus status;

    private Class<SagaTx> preExecutedTx;
    private Class<SagaTx> preCompensatedTx;

    private Map<String, Object> transientCache = new HashMap<>();
    private Map<String, Object> persistentCache = new HashMap<>();

    SagaContext(Saga saga) {
        this.saga = saga;
    }

    public static SagaContext getContext(Long id) {
        // TODO
        return null;
    }

    public Saga getSaga() {
        return saga;
    }

    public Long getId() {
        return this.id;
    }

    void setId(Long id) {
        this.id = id;
    }

    public SagaStatus getStatus() {
        return status;
    }

    void setStatus(SagaStatus status) {
        this.status = status;
    }

    private String getBizId() {
        return this.bizId;
    }

    void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public Object getBizParam() {
        return bizParam;
    }

    void setBizParam(Object bizParam) {
        this.bizParam = bizParam;
    }

    public Object getBizResult() {
        return bizResult;
    }

    public void setBizResult(Object bizResult) {
        this.bizResult = bizResult;
    }

    public Object putTransientCache(String key, Object value) {
        return transientCache.put(key, value);
    }

    public Object getTransientCache(String key) {
        return transientCache.get(key);
    }

    public Object putPersistentCache(String key, Object value) {
        // TODO store
        return persistentCache.put(key, value);
    }

    public Object getPersistentCache(String key) {
        // TODO store
        return persistentCache.get(key);
    }

    public Class<SagaTx> getPreExecutedTx() {
        return preExecutedTx;
    }

    public Class<SagaTx> getPreCompensatedTx() {
        return preCompensatedTx;
    }
}
