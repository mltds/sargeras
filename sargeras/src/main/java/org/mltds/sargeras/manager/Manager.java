package org.mltds.sargeras.manager;

import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaResult;
import org.mltds.sargeras.api.spi.SagaBean;
import org.mltds.sargeras.exception.SagaContextLockFailException;

/**
 * 这个服务类是给Saga内部使用的，不建议外部系统直接使用。
 * 
 * @author sunyi
 */
public interface Manager extends SagaBean {

    /**
     * 执行一个新的 Saga
     * 
     * @param saga Saga 对象
     * @param bizId 业务ID
     * @param bizParam 业务参数
     * @return Saga的状态和执行结果
     */
    SagaResult start(Saga saga, String bizId, Object bizParam) throws SagaContextLockFailException;

    /**
     * 手动启动一个执行中的Saga
     */
    SagaResult restart(Saga saga, String bizId) throws SagaContextLockFailException;

    /**
     * 轮询重试一个执行中的Saga，如果获取锁失败或早于下一次触发时间（nextTriggerTime），则不会执行。
     */
    void pollRetry(long contextId);

}
