package org.mltds.sargeras.server.facade;

import org.mltds.sargeras.common.core.SagaContextBase;
import org.mltds.sargeras.common.core.SagaStatus;

import java.util.Date;
import java.util.List;

/**
 * @author sunyi.
 */
public interface ServerFacade {

    /**
     * 作为一条新记录去创建并获取锁，如果 ID 或 BizId 已存在则报错
     */
    void saveContextAndLock(SagaContextBase context, int lockTimeout);

    /**
     * 根据 ContextId 从存储的信息中，重新构建出一个 {@link SagaContextBase}
     */
    SagaContextBase loadContext(long contextId);

    /**
     * 根据 BizId 从存储的信息中，重新构建出一个 {@link SagaContextBase}
     */
    SagaContextBase loadContext(String appName, String bizName, String bizId);

    /**
     * 保存状态并更新ModifyTime
     */
    void saveContextStatus(long contextId, SagaStatus status);

    /**
     * 保存当前要执行/执行中的TX
     */
    void saveCurrentTx(long contextId, String clsName);

    /**
     * 保存上一个执行完的TX
     */
    void savePreExecutedTx(long contextId, String clsName);

    /**
     * 保存上一个补偿完的TX
     */
    void savePreCompensatedTx(long contextId, String clsName);

    /**
     * 触发次数自增加一
     */
    void incrementTriggerCount(long contextId);

    /**
     * 保存下一次期望的触发时间
     */
    void saveNextTriggerTime(long contextId, Date nextTriggerTime);

    /**
     * 保存Context的信息
     */
    void saveContextInfo(long contextId, String key, Object info);

    /**
     * 读取上下文信息
     */
    <T> T loadContextInfo(long contextId, String key, Class<T> cls);

    /**
     * 获取锁
     */
    boolean lock(long id, String reqId, int timeoutSec);

    /**
     * 释放锁
     */
    boolean unlock(long id, String reqId);

    /**
     * 查询出需要轮询重试的 Context Id List
     *
     * @param limit 返回最多条数，参见数据库的 limit。
     * @return Context ID 的集合
     */
    List<Long> findNeedRetryContextList(int limit);


}
