package org.mltds.sargeras.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mltds.sargeras.api.spi.SagaBean;
import org.mltds.sargeras.api.spi.SagaBeanFactory;
import org.mltds.sargeras.exception.SagaException;
import org.mltds.sargeras.exception.SagaNotFoundException;
import org.mltds.sargeras.manager.Manager;
import org.mltds.sargeras.repository.Repository;
import org.mltds.sargeras.serialize.Serialize;

/**
 * @author sunyi
 */
public class SagaApplication {

    private static final Map<Class, SagaBeanFactory> factories = new ConcurrentHashMap<>();
    private static final Map<String, Saga> sagas = new ConcurrentHashMap<>();

    static void addSaga(Saga saga) {
        String keyName = saga.getKeyName();
        if (sagas.containsKey(keyName)) {
            throw new SagaException("已经存在一个相同的Saga，同名的Saga：" + keyName);
        }
        sagas.put(keyName, saga);
    }

    static void addBeanFactory(Class<? extends SagaBean> beanClass, SagaBeanFactory beanFactory) {
        factories.put(beanClass, beanFactory);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SagaBean> T getBean(Class<T> beanClass) {
        SagaBeanFactory beanFactory = factories.get(beanClass);
        return (T) beanFactory.getObject();
    }

    public static Manager getManager() {
        return getBean(Manager.class);
    }

    public static Repository getRepository() {
        return getBean(Repository.class);
    }

    public static Serialize getSerialize() {
        return getBean(Serialize.class);
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

}
