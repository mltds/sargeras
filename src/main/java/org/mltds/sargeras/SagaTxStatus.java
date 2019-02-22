package org.mltds.sargeras;

/**
 * 
 * @author sunyi 2019/2/15.
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
     * 失败，只可用于 {@link SagaTx#execute(SagaContext)} 。 <br/>
     * {@link SagaTx#compensate(SagaContext)} 理论上没有失败，如果暂时无法补偿可以设置为处理中，哪怕需要人工介入直至成功。
     */
    FAIL_TO_COMPENSATE
}