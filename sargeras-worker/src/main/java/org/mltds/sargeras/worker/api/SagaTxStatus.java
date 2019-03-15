package org.mltds.sargeras.worker.api;

/**
 * 
 * @author sunyi
 */
public enum SagaTxStatus {

    /**
     * 成功，执行或补偿下一个 TX
     */
    SUCCESS,

    /**
     * 处理中，流程挂起，计算下一次期望重试的时间点，等待轮询重试或手动触发。
     */
    PROCESSING,

    /**
     * 失败，如果是执行（Execute）失败，则转为补偿（Compensate）流程；如果是补偿失败，则流程终止。
     */
    FAILURE
}