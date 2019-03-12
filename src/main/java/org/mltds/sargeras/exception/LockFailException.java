package org.mltds.sargeras.exception;

/**
 * @author sunyi.
 */
public class LockFailException extends SagaException {

    public LockFailException(Long contextId, String onceId) {
        super("获取锁失败, Context Id: " + contextId + ", Once Id: " + onceId);
    }

}
