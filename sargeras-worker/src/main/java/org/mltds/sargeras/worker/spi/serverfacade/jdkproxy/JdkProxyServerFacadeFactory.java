package org.mltds.sargeras.worker.spi.serverfacade.jdkproxy;

import java.lang.reflect.Proxy;

import org.mltds.sargeras.server.facade.ServerFacade;
import org.mltds.sargeras.worker.spi.serverfacade.ServerFacadeFactory;

/**
 * @author sunyi.
 */
public class JdkProxyServerFacadeFactory implements ServerFacadeFactory {

    private ServerFacade serverFacade;

    @Override
    public ServerFacade getObject() {

        if (serverFacade != null) {
            return serverFacade;
        }

        synchronized (JdkProxyServerFacadeFactory.class) {
            if (serverFacade != null) {
                return serverFacade;
            }

            JdkProxyServerFacade jdkProxyServerFacade = new JdkProxyServerFacade();
            ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
            serverFacade = (ServerFacade) Proxy.newProxyInstance(clsLoader, new Class[] { ServerFacade.class }, jdkProxyServerFacade);
            return serverFacade;
        }
    }
}