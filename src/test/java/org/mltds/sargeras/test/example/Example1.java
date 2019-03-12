package org.mltds.sargeras.test.example;

import java.util.UUID;

import org.junit.Test;
import org.mltds.sargeras.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi
 */

public class Example1 {

    public static final Logger logger = LoggerFactory.getLogger(Example1.class);

    @Test
    public void test() {

        Saga saga = SagaBuilder.newBuilder("TestApp", "Example1").addTx(new Tx1()).addListener(new Listener()).build();
        SagaLauncher.launch();

        String bizId = UUID.randomUUID().toString().replace("-", "");

        saga.start(bizId, null);

    }

    private static class Listener implements SagaListener {

        @Override
        public void onStart(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "#onStart");
        }

        @Override
        public void onRestart(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "#onRestart");
        }

        @Override
        public void onToComp(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "#onToComp");
        }

        @Override
        public void onToFinal(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "#onToFinal");
        }

        @Override
        public void beforeExecute(SagaContext context, SagaTx tx) {
            logger.info(context.getSaga().getKeyName() + "#beforeExecute");
        }

        @Override
        public void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status) {
            logger.info(context.getSaga().getKeyName() + "#afterExecute");
        }

        @Override
        public void beforeCompensate(SagaContext context, SagaTx tx) {
            logger.info(context.getSaga().getKeyName() + "#beforeCompensate");
        }

        @Override
        public void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status) {
            logger.info(context.getSaga().getKeyName() + "#afterCompensate");
        }

        @Override
        public void onException(SagaContext context, Throwable t) {
            logger.error(context.getSaga().getKeyName() + "#onException", t);
        }
    }

    private static class Tx1 implements SagaTx {
        @Override
        public SagaTxStatus execute(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "eeeeeeeeeeexecute Tx1 ..............");
            logger.info(String.valueOf(context.getBizParam() == null));
            return SagaTxStatus.SUCCESS;
        }

        @Override
        public SagaTxStatus compensate(SagaContext context) {
            logger.info(context.getSaga().getKeyName() + "cccccccccccompensate Tx1 ..............");
            return SagaTxStatus.SUCCESS;
        }
    }

}
