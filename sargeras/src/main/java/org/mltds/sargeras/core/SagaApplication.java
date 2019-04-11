package org.mltds.sargeras.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.SagaNotFoundException;
import org.mltds.sargeras.api.listener.SagaListener;
import org.mltds.sargeras.api.listener.SagaListenerChain;
import org.mltds.sargeras.api.listener.SagaTxListener;
import org.mltds.sargeras.api.listener.SagaTxListenerChain;
import org.springframework.stereotype.Component;

/**
 * @author sunyi
 */
@Component
public class SagaApplication {

    private final Map<String, Saga> sagas = new ConcurrentHashMap<>();

    private List<SagaListener> sagaListenerList;
    private SagaListenerChain sagaListenerChain;

    private List<SagaTxListener> sagaTxListenerList;
    private SagaTxListenerChain sagaTxListenerChain;

    @PostConstruct
    public void init() {
        sagaListenerChain = new SagaListenerChain(sagaListenerList);
        sagaTxListenerChain = new SagaTxListenerChain(sagaTxListenerList);
    }

    void addSaga(Saga saga) {
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
        Saga saga = sagas.get(keyName);
        if (saga == null) {
            throw new SagaNotFoundException(keyName);
        }
        return saga;
    }

    public Saga getSaga(String appName, String bizName) {
        return getSaga(Saga.getKeyName(appName, bizName));
    }

    public void setSagaListenerList(List<SagaListener> sagaListenerList) {
        this.sagaListenerList = sagaListenerList;
    }

    public void setSagaTxListenerList(List<SagaTxListener> sagaTxListenerList) {
        this.sagaTxListenerList = sagaTxListenerList;
    }

    public SagaListenerChain getSagaListenerChain() {
        return sagaListenerChain;
    }

    public SagaTxListenerChain getSagaTxListenerChain() {
        return sagaTxListenerChain;
    }
}
