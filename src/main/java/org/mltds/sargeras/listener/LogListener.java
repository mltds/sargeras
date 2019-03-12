package org.mltds.sargeras.listener;

import org.mltds.sargeras.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sunyi.
 */
public class LogListener implements SagaListener {

    private static final Logger logger = LoggerFactory.getLogger(SagaConfig.getProperty(SagaConfig.LISTENER_LOGGER_NAME));

    @Override
    public void onStart(SagaContext context) {
        logger.info("Saga 启动执行. KeyName:{}, ContextId:{},BizId:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId());
    }

    @Override
    public void onRestart(SagaContext context) {
        logger.info("Saga 重新启动. KeyName:{}, ContextId:{},BizId:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId());
    }

    @Override
    public void onToComp(SagaContext context) {
        logger.info("Saga 开始补偿. KeyName:{}, ContextId:{},BizId:{},PreExecutedTx:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                context.getPreExecutedTx().getSimpleName());
    }

    @Override
    public void onToFinal(SagaContext context) {
        logger.info("Saga 失败终止. KeyName:{}, ContextId:{},BizId:{},PreCompensatedTx:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                context.getPreCompensatedTx().getSimpleName());
    }

    @Override
    public void beforeExecute(SagaContext context, SagaTx tx) {
        logger.info("SagaTx执行前. KeyName:{}, ContextId:{},BizId:{},TX:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName());
    }

    @Override
    public void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status) {
        logger.info("SagaTx执行后. KeyName:{}, ContextId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName(), status);
    }

    @Override
    public void beforeCompensate(SagaContext context, SagaTx tx) {
        logger.info("SagaTx补偿前. KeyName:{}, ContextId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName());
    }

    @Override
    public void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status) {
        logger.info("SagaTx补偿后. KeyName:{}, ContextId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getId(), context.getBizId(),
                tx.getClass().getSimpleName(), status);
    }

    @Override
    public void onException(SagaContext context, Throwable t) {
        logger.warn("Saga执行过程中发生异常. KeyName:{}, ContextId:{},BizId:{}", new Object[] { context.getSaga().getKeyName(), context.getId(), context.getBizId() },
                t);
    }
}
