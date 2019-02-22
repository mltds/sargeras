package org.mltds.sargeras;

/**
 * @author sunyi 2019/2/15.
 */
public interface SagaTx {

    /**
     * 必须支持幂等和重试
     */
    SagaTxStatus execute(SagaContext context);

    /**
     * 必须支持幂等和重试
     * @return 不能返回 {@link SagaTxStatus#FAIL_TO_COMPENSATE}
     */
    SagaTxStatus compensate(SagaContext context);

}