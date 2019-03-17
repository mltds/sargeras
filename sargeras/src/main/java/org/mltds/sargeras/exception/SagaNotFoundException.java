package org.mltds.sargeras.exception;

/**
 * @author sunyi.
 */
public class SagaNotFoundException extends SagaException {

    public SagaNotFoundException(String keyName) {
        super("Saga 没有找到，KeyName：" + keyName);
    }

    public SagaNotFoundException(String appName, String bizName) {
        super("Saga 没有找到，AppName：" + appName + "，BizName：" + bizName);
    }

}
