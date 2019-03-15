package org.mltds.sargeras.common.exception;

/**
 * @author sunyi.
 */
public class SagaContextLockFailException extends SagaException {

    public SagaContextLockFailException(Long contextId, String onceId) {
        super("获取锁失败, Context Id: " + contextId + ", Once Id: " + onceId);
    }

}
