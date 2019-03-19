package org.mltds.sargeras.api.exception;

/**
 * 当进行重启或重试时，找不到执行/补偿的起始点时，会抛出此异常。<br/>
 * 一般是因为代码变动，导致新老流程不兼容的情况。
 * 
 * @author sunyi.
 */
public class SagaIncompatibleException extends SagaException {

    public SagaIncompatibleException(Long contextId) {
        super("Saga流程不兼容, ContextId: " + contextId);
    }

}
