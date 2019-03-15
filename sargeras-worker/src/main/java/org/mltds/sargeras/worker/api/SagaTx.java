package org.mltds.sargeras.worker.api;

/**
 * @author sunyi
 */
public interface SagaTx {

    /**
     * 正向执行，必须支持幂等和重试
     * 
     * @return 必须返回一个状态
     */
    SagaTxStatus execute(SagaContext context);

    /**
     * 逆向补偿，必须支持幂等和重试
     * 
     * @return 必须返回一个状态
     */
    SagaTxStatus compensate(SagaContext context);

}