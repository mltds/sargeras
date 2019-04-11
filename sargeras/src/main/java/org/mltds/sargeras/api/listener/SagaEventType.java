package org.mltds.sargeras.api.listener;

/**
 * @author sunyi.
 */
public enum SagaEventType {

    /**
     * 当 Saga 触发，包括首次和重试
     */
    ON_TRIGGER,

    /**
     * 当 Saga 执行成功
     */
    ON_EXECUTE_SUCCESS,

    /**
     * 当 Saga 补偿成功
     */
    ON_COMPENSATE_SUCCESS,

    /**
     * 当 Saga 补偿失败
     */
    ON_COMPENSATE_FAILURE,

    /**
     * 当 Saga 业务超时
     */
    ON_OVERTIME

}
