package org.mltds.sargeras.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.spi.pollretry.PollRetry;
import org.springframework.stereotype.Component;

/**
 * @author sunyi
 */
@Component
public class SagaApplication {

    private final Map<String, Saga> sagas = new ConcurrentHashMap<>();

    private PollRetry pollRetry;

    public void addSaga(Saga saga) {
        synchronized (SagaApplication.class) {
            String keyName = saga.getKeyName();
            if (!sagas.containsKey(keyName)) {
                sagas.put(keyName, saga);
            } else if (!sagas.get(keyName).equals(saga)) {
                throw new SagaException("不能有同名的Saga，KeyName：" + keyName);
            }
        }
    }

    public Saga getSaga(String keyName) {
        return sagas.get(keyName);
    }

    public Saga getSaga(String appName, String bizName) {
        return getSaga(Saga.getKeyName(appName, bizName));
    }

    public void after() {

    }

}
