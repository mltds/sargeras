package org.mltds.sargeras.worker.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mltds.sargeras.common.exception.SagaException;
import org.mltds.sargeras.server.facade.ServerFacade;
import org.mltds.sargeras.worker.spi.SagaBeanFactory;
import org.mltds.sargeras.worker.spi.manager.Manager;
import org.mltds.sargeras.worker.spi.network.Network;
import org.mltds.sargeras.worker.spi.pollretry.PollRetry;

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

    public static Saga getSaga(String keyName) {
        return sagas.get(keyName);
    }

    public static Saga getSaga(String appName, String bizName) {
        return getSaga(Saga.getKeyName(appName, bizName));
    }

    static void addBeanFactory(Class<?> beanClass, SagaBeanFactory beanFactory) {
        factories.put(beanClass, beanFactory);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getBean(Class<T> beanClass) {
        SagaBeanFactory beanFactory = factories.get(beanClass);
        return (T) beanFactory.getObject();
    }

    public static Manager getManager() {
        return getBean(Manager.class);
    }

    public static ServerFacade getServerFacade() {
        return getBean(ServerFacade.class);
    }

    public static Network getNetwork() {
        return getBean(Network.class);
    }

    public static PollRetry getPollRetry() {
        return getBean(PollRetry.class);
    }
}
