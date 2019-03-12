package org.mltds.sargeras.repository;

import org.mltds.sargeras.api.SagaBean;
import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTx;

/**
 * @author sunyi
 */
public interface Repository extends SagaBean {

    /**
     * 如果 ID 在存储中存在则更新，否则作为一条新记录去创建。
     */
    Long saveContext(SagaContext context);

    void saveContextStatus(Long contextId, SagaStatus status);

    void saveCurrentTx(Long contextId, Class<? extends SagaTx> cls);

    void savePreExecutedTx(Long contextId, Class<? extends SagaTx> cls);

    void savePreCompensatedTx(Long contextId, Class<? extends SagaTx> cls);

    /**
     * 根据ID从存储的信息中，重新构建出一个 {@link SagaContext}
     */
    SagaContext loadContext(Long id);

    void saveContextInfo(Long contextId, String key, Object info);

    <T> T loadContextInfo(Long contextId, String key, Class<T> cls);

    boolean lock(Long id, String reqId, int timeoutSec);

    boolean unlock(Long id, String reqId);

}