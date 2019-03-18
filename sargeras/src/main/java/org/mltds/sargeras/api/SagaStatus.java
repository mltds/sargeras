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
     * 流程一直处理中直到超过了既定的 biz_timeout，最终结果未知，不再继续轮询重试。
     */
    OVERTIME,

    /**
     * 不兼容的，比如在代码变更后，无法重试某个执行中的流程，会标记为不兼容状态，不再继续轮询重试。<br/>
     * 暂时没有使用，因为不兼容的话就是在 Restart 时找不到起始点，会让线程空跑一次，考虑到如果直接变为终态可能有风险。
     */
    INCOMPATIBLE;

}
