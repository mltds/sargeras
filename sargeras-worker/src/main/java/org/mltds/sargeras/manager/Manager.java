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
     * 立刻启动一个执行中的Saga
     * 
     * @throws SagaContextLockFailException 如果获取锁失败
     */
    SagaResult restart(Saga saga, String bizId) throws SagaContextLockFailException;

    /**
     * 重试一个执行中的Saga，与 {@link #restart(Saga, String)} 的区别为：
     * <ul>
     * <li>获取锁失败不会抛出异常</li>
     * <li>如果获取锁失败或早于下一次触发时间（nextTriggerTime），则不会执行</li>
     * </ul>
     */
    SagaResult retry(long contextId);

}
