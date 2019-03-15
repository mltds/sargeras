package org.mltds.sargeras.common.exception;

/**
 * @author sunyi.
 */
public class SagaNotFoundException extends SagaException {

    public SagaNotFoundException(String appName, String bizName) {
        super("Saga 没有找到，AppName：" + appName + "，BizName：" + bizName);
    }

}
