package org.mltds.sargeras.api;

/**
 * 
 * @author sunyi
 */
public enum SagaTxStatus {

    /**
     * 执行成功，执行下一个 TX
     */
    SUCCESS,

    /**
     * 处理中，流程挂起，计算下一次期望重试的时间点，等待轮询重试或手动触发。
     */
    PROCESSING,

    /**
     * 执行（Execute）失败，转为补偿（Compensate）流程。
     */
    FAILURE,

    /**
     * 补偿成功，补偿下一个 TX
     */
    COMPENSATE_SUCCESS,

    /**
     * 处理中，流程挂起，计算下一次期望重试的时间点，等待轮询重试或手动触发。
     */
    COMPENSATE_PROCESSING,

    /**
     * 补偿失败，流程终止。
     */
    COMPENSATE_FAILURE,

}