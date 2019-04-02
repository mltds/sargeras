//package org.mltds.sargeras.api.listener;
//
//import org.mltds.sargeras.api.SagaConfig;
//import org.mltds.sargeras.core.SagaContext;
//import org.mltds.sargeras.api.SagaTx;
//import org.mltds.sargeras.api.SagaTxStatus;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * 提供一个只记 log 的Listener，简单实用。
// *
// * @author sunyi.
// */
//public class LogListener implements SagaListener {
//
//    private static final Logger logger = LoggerFactory.getLogger(SagaConfig.getProperty(SagaConfig.LISTENER_LOGGER_NAME));
//
//    @Override
//    public void onStart(SagaContext context) {
//        logger.info("Saga 开始执行. KeyName:{},RecordId:{},BizId:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId());
//    }
//
//    @Override
//    public void onRestart(SagaContext context) {
//        logger.info("Saga 重新启动. KeyName:{},RecordId:{},BizId:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId());
//    }
//
//    @Override
//    public void onExecuteSucc(SagaContext context) {
//        logger.info("Saga 执行成功. KeyName:{},RecordId:{},BizId:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId());
//    }
//
//    @Override
//    public void onCompensateSucc(SagaContext context) {
//        logger.info("Saga 补偿成功. KeyName:{},RecordId:{},BizId:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId());
//    }
//
//    @Override
//    public void onExeFailToComp(SagaContext context) {
//        logger.warn("Saga 执行失败，开始补偿. KeyName:{},RecordId:{},BizId:{},PreExecutedTx:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId(),
//                context.getPreExecutedTx().getSimpleName());
//    }
//
//    @Override
//    public void onComFailToFinal(SagaContext context) {
//        logger.warn("Saga 补偿失败，流程终止. KeyName:{},RecordId:{},BizId:{},PreCompensatedTx:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId(),
//                context.getPreCompensatedTx().getSimpleName());
//    }
//
//    @Override
//    public void beforeExecute(SagaContext context, SagaTx tx) {
//        logger.debug("SagaTx 执行前. KeyName:{},RecordId:{},BizId:{},TX:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId(),
//                tx.getClass().getSimpleName());
//    }
//
//    @Override
//    public void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status) {
//        logger.debug("SagaTx 执行后. KeyName:{},RecordId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId(),
//                tx.getClass().getSimpleName(), status);
//    }
//
//    @Override
//    public void beforeCompensate(SagaContext context, SagaTx tx) {
//        logger.debug("SagaTx 补偿前. KeyName:{},RecordId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId(),
//                tx.getClass().getSimpleName());
//    }
//
//    @Override
//    public void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status) {
//        logger.debug("SagaTx 补偿后. KeyName:{},RecordId:{},BizId:{},Tx:{}，TxStatus:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId(),
//                tx.getClass().getSimpleName(), status);
//    }
//
//    @Override
//    public void onOvertime(SagaContext context) {
//        logger.warn("Saga 业务超时，不再跟踪. KeyName:{},RecordId:{},BizId:{}", context.getSaga().getKeyName(), context.getRecordId(), context.getBizId());
//    }
//
//    @Override
//    public void onException(SagaContext context, Throwable t) {
//        logger.error("Saga 执行过程中发生异常. KeyName:{" + context.getSaga().getKeyName() + "},RecordId:{" + context.getRecordId() + "},BizId:{" + context.getBizId() + "}",
//                t);
//    }
//}
