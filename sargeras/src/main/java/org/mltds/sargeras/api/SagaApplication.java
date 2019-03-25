package org.mltds.sargeras.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.SagaNotFoundException;
import org.mltds.sargeras.spi.manager.Manager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author sunyi
 */
public class SagaApplication implements ApplicationContextAware {

    private static final Map<String, Saga> sagas = new ConcurrentHashMap<>();

    private static Manager manager;

    static void addSaga(Saga saga) {
        synchronized (SagaApplication.class) {
            String keyName = saga.getKeyName();
            if (!sagas.containsKey(keyName)) {
                sagas.put(keyName, saga);
            } else if (!sagas.get(keyName).equals(saga)) {
                throw new SagaException("不能有同名的Saga，KeyName：" + keyName);
            }
        }
    }

    public static Saga getSaga(String keyName) {
        Saga saga = sagas.get(keyName);
        if (saga == null) {
            throw new SagaNotFoundException(keyName);
        }
        return saga;
    }

    public static Saga getSaga(String appName, String bizName) {
        return getSaga(Saga.getKeyName(appName, bizName));
    }

    public static Manager getManager() {
        return manager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        manager = applicationContext.getBean(Manager.class);
    }
}
