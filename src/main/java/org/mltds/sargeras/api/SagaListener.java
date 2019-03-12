package org.mltds.sargeras.api;

/**
 * Listener 用于监听各个事件或动作。发生的异常都会被
 * 
 * @author sunyi
 */
public interface SagaListener {

    /**
     * 当首次执行时触发的事件
     */
    void onStart(SagaContext context);

    /**
     * 当再次启动并轮训重试时，触发的事件。<br/>
     * PS：某个TX返回状态为WAIT，那么过一段时间会再次轮训重试这个TX，重试前会触发这个事件
     *
     */
    void onRestart(SagaContext context);

    void onToComp(SagaContext context);

    void onToFinal(SagaContext context);

    void beforeExecute(SagaContext context, SagaTx tx);

    void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status);

    void beforeCompensate(SagaContext context, SagaTx tx);

    void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status);

    /**
     * 当发生异常时
     */
    void onException(SagaContext context, Throwable t);

}