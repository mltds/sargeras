package org.mltds.sargeras.repository;

import java.util.Date;
import java.util.List;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.spi.SagaBean;

/**
 * @author sunyi
 */
public interface Repository extends SagaBean {

    /**
     * 如果 ID 在存储中存在则更新，否则作为一条新记录去创建。
     */
    long saveContext(SagaContext context);

    /**
     * 根据 ContextId 从存储的信息中，重新构建出一个 {@link SagaContext}
     */
    SagaContext loadContext(long contextId);

    /**
     * 根据 BizId 从存储的信息中，重新构建出一个 {@link SagaContext}
     */
    SagaContext loadContext(String appName, String bizName, String bizId);

    /**
     * 保存状态并更新ModifyTime
     */
    void saveContextStatus(long contextId, SagaStatus status);

    /**
     *
     */
    void saveCurrentTx(long contextId, Class<? extends SagaTx> cls);

    void savePreExecutedTx(long contextId, Class<? extends SagaTx> cls);

    void savePreCompensatedTx(long contextId, Class<? extends SagaTx> cls);

    void incrementTriggerCount(long contextId);

    void saveNextTriggerTime(long contextId, Date nextTriggerTime);

    void saveContextInfo(long contextId, String key, Object info);

    <T> T loadContextInfo(long contextId, String key, Class<T> cls);

    boolean lock(long id, String reqId, int timeoutSec);

    boolean unlock(long id, String reqId);

    List<Long> findNeedRetryContextList(int limit);

}