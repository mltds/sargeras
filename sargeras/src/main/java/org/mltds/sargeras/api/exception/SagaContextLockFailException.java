package org.mltds.sargeras.api.exception;

/**
 * @author sunyi.
 */
public class SagaContextLockFailException extends SagaException {

    public SagaContextLockFailException(Long contextId, String triggerId) {
        super("获取锁失败, ContextId: " + contextId + ", TriggerId: " + triggerId);
    }

}
