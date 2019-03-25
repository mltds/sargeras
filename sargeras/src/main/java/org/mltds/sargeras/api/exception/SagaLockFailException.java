package org.mltds.sargeras.api.exception;

/**
 * @author sunyi.
 */
public class SagaLockFailException extends SagaException {

    public SagaLockFailException(Long recordId, String triggerId) {
        super("获取锁失败, RecordId: " + recordId + ", TriggerId: " + triggerId);
    }

}
