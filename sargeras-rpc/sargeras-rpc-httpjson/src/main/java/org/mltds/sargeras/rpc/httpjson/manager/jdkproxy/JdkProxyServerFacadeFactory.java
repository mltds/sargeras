package org.mltds.sargeras.rpc.httpjson.manager.jdkproxy;

import java.lang.reflect.Proxy;

import org.mltds.sargeras.server.facade.ServerFacade;
import org.mltds.sargeras.rpc.httpjson.manager.ServerFacadeFactory;
import org.mltds.sargeras.spi.manager.ManagerFactory;

/**
 * @author sunyi.
 */
public class JdkProxyServerFacadeFactory implements ManagerFactory {

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