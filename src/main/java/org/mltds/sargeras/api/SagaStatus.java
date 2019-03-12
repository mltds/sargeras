package org.mltds.sargeras.api;

/**
 * @author sunyi
 */
public enum SagaStatus {

    /**
     * 处理中，初始化状态
     */
    INIT,

    /**
     * 处理中，正向执行中
     */
    EXECUTING,

    /**
     * 处理中，逆向补偿中
     */
    COMPENSATING,

    /**
     * 所有 TX 都执行成功，Saga 最终执行成功
     */
    EXECUTE_SUCC,

    /**
     * 所有需要补偿的 TX 都补偿成功，Saga 最终补偿成功。
     */
    COMPENSATE_SUCC,

    /**
     * 某个需要补偿的 TX 补偿失败，Saga 最终失败。
     */
    COMPENSATE_FAIL,

    /**
     * 一直处理中，直到超过了既定的 biz_timeout，最终结果未知，不再跟进。
     */
    OVERTIME;

}
