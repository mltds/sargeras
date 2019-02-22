package org.mltds.sargeras.test.example;

import java.util.UUID;

import org.mltds.sargeras.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi 2019/2/20.
 */
public class Example1 {

    public static final Logger logger = LoggerFactory.getLogger(Example1.class);

    public static void main(String[] args) {

        Saga saga = SagaBuilder.newBuilder("TestApp", "Example1").addTx(new Tx1()).addListener(new Listener()).build();

        String bizId = UUID.randomUUID().toString().replace("-", "");

        saga.run(bizId, "123");





    }

    private static class Listener implements SagaListener {

        @Override
        public void onExecuteStart(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "#onExecuteStart");
        }

        @Override
        public void onCompensateStart(SagaContext context) {
            logger.warn(context.getSaga().getKeyName() + "#onCompensateStart");
        }

        @Override
        public void onPollRetry(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "#onPollRetry");
        }

        @Override
        public SagaTxStatus onException(Throwable t, SagaContext context) {
            logger.error(context.getSaga().getKeyName() + "#onException", t);
            return SagaTxStatus.PROCESSING;
        }

    }

    private static class Tx1 implements SagaTx {
        @Override
        public SagaTxStatus execute(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "eeeeeeeeeeexecute Tx1 ..............");
            return SagaTxStatus.SUCCESS;
        }

        @Override
        public SagaTxStatus compensate(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "cccccccccccompensate Tx1 ..............");
            return SagaTxStatus.SUCCESS;
        }
    }

}
