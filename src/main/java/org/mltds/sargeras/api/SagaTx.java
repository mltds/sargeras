package org.mltds.sargeras.api;

/**
 * @author sunyi
 */
public interface SagaTx {

    /**
     * 正向执行，必须支持幂等和重试
     *
     * @return 不能返回 {@link SagaTxStatus#COMP_FAIL_TO_FINAL}
     */
    SagaTxStatus execute(SagaContext context);

    /**
     * 逆向补偿，必须支持幂等和重试
     * 
     * @return 不能返回 {@link SagaTxStatus#EXE_FAIL_TO_COMP}
     */
    SagaTxStatus compensate(SagaContext context);

}