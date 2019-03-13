package org.mltds.sargeras.listener;

import org.mltds.sargeras.api.SagaConfig;
import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供一个只记 log 的Listener，简单实用。
 * 
 * @author sunyi.
 */
public class LogListener implements SagaListener {

    private static final Logger logger = LoggerFactory.getLogger(SagaConfig.getProperty(SagaConfig.LISTENER_LOGGER_NAME));

    @Override
    public void onStart(SagaContext context) {
        logger.info("Saga 开始执行. KeyName:{}, ContextId:{},BizId:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId());
    }

    @Override
    public void onRestart(SagaContext context) {
        logger.info("Saga 重新启动. KeyName:{}, ContextId:{},BizId:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId());
    }

    @Override
    public void onExeFailToComp(SagaContext context) {
        logger.warn("Saga 执行失败，开始补偿. KeyName:{}, ContextId:{},BizId:{},PreExecutedTx:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                context.getPreExecutedTx().getSimpleName());
    }

    @Override
    public void onComFailToFinal(SagaContext context) {
        logger.warn("Saga 补偿失败，流程终止. KeyName:{}, ContextId:{},BizId:{},PreCompensatedTx:{}", context.getSaga().getKeyName(), context.getId(),
                context.getBizId(), context.getPreCompensatedTx().getSimpleName());
    }

    @Override
    public void beforeExecute(SagaContext context, SagaTx tx) {
        logger.debug("SagaTx 执行前. KeyName:{}, ContextId:{},BizId:{},TX:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName());
    }

    @Override
    public void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status) {
        logger.debug("SagaTx 执行后. KeyName:{}, ContextId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName(), status);
    }

    @Override
    public void beforeCompensate(SagaContext context, SagaTx tx) {
        logger.debug("SagaTx 补偿前. KeyName:{}, ContextId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName());
    }

    @Override
    public void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status) {
        logger.debug("SagaTx 补偿后. KeyName:{}, ContextId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName(), status);
    }

    @Override
    public void onOvertime(SagaContext context) {
        logger.warn("Saga 业务超时，不再跟踪. KeyName:{}, ContextId:{},BizId:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId());
    }

    @Override
    public void onException(SagaContext context, Throwable t) {
        logger.error("Saga 执行过程中发生异常. KeyName:{" + context.getSaga().getKeyName() + "}, ContextId:{" + context.getId() + "},BizId:{" + context.getBizId() + "}",
                t);
    }
}
