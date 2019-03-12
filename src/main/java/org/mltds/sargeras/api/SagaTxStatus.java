package org.mltds.sargeras.api;

/**
 * 
 * @author sunyi
 */
public enum SagaTxStatus {

    /**
     * 成功，可用于 {@link SagaTx#execute(SagaContext)} 和 {@link SagaTx#compensate(SagaContext)}
     */
    SUCCESS,

    /**
     * 处理中，可用于 {@link SagaTx#execute(SagaContext)} 和 {@link SagaTx#compensate(SagaContext)}
     */
    PROCESSING,

    /**
     * 执行失败，从正向执行流程转为逆向补偿流程，只可用于 {@link SagaTx#execute(SagaContext)} 。 <br/>
     * {@link SagaTx#compensate(SagaContext)} 理论上没有失败，如果暂时无法补偿可以设置为处理中，哪怕需要人工介入直至成功。
     */
    EXE_FAIL_TO_COMP,

    /**
     * 补偿失败，流程中止，只可用于{@link SagaTx#compensate(SagaContext)} 。
     */
    COMP_FAIL_TO_FINAL;
}