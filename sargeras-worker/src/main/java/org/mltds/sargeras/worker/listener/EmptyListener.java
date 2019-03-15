package org.mltds.sargeras.worker.listener;

import org.mltds.sargeras.worker.api.SagaContext;
import org.mltds.sargeras.worker.api.SagaTx;
import org.mltds.sargeras.worker.api.SagaTxStatus;

/**
 * Listener 的方法比较多，为了方便用户可以只关注想要关注的事件，所以提供了一个空的Listener，用于作为父类。
 * 
 * @author sunyi.
 */
public class EmptyListener implements SagaListener {
    @Override
    public void onStart(SagaContext context) {

    }

    @Override
    public void onRestart(SagaContext context) {

    }

    @Override
    public void onExecuteSucc(SagaContext context) {

    }

    @Override
    public void onCompensateSucc(SagaContext context) {

    }

    @Override
    public void onOvertime(SagaContext context) {

    }

    @Override
    public void onException(SagaContext context, Throwable t) {

    }

    @Override
    public void onExeFailToComp(SagaContext context) {

    }

    @Override
    public void onComFailToFinal(SagaContext context) {

    }

    @Override
    public void beforeExecute(SagaContext context, SagaTx tx) {

    }

    @Override
    public void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status) {

    }

    @Override
    public void beforeCompensate(SagaContext context, SagaTx tx) {

    }

    @Override
    public void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status) {

    }
}
