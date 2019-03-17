package org.mltds.sargeras.api.listener;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;

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
     * PS：某个 {@link SagaTx} 返回状态为 {@link SagaTxStatus#PROCESSING}，那么过一段时间会再次轮训重试这个TX，重试前会触发这个事件
     *
     */
    void onRestart(SagaContext context);

    /**
     * 当全部正向执行后
     */
    void onExecuteSucc(SagaContext context);

    /**
     * 当需要补偿的，全部补偿成功后
     */
    void onCompensateSucc(SagaContext context);

    /**
     * 当超时后，不再跟踪。
     */
    void onOvertime(SagaContext context);

    /**
     * 当发生异常时
     */
    void onException(SagaContext context, Throwable t);

    /**
     * 当执行失败，进行补偿时
     */
    void onExeFailToComp(SagaContext context);

    /**
     * 当补偿失败，流程终止时
     */
    void onComFailToFinal(SagaContext context);

    /**
     * 执行前
     */
    void beforeExecute(SagaContext context, SagaTx tx);

    /**
     * 执行后
     */
    void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status);

    /**
     * 补偿前
     */
    void beforeCompensate(SagaContext context, SagaTx tx);

    /**
     * 补偿后
     */
    void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status);

}