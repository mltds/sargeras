package org.mltds.sargeras;

/**
 * @author sunyi 2019/2/19.
 */
public interface SagaListener {

    void onExecuteStart(SagaContext context);

    void onCompensateStart(SagaContext context);

    /**
     * 当再次启动并轮训重试时，触发的事件。<br/>
     * PS：某个TX返回状态为WAIT，那么过一段时间会再次轮训重试这个TX，重试前会触发这个事件
     *
     */
    void onPollRetry(SagaContext context);

    /**
     * 当执行业务流程
     */
    SagaTxStatus onException(Throwable t, SagaContext context);

}
