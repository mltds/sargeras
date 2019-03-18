package org.mltds.sargeras.spi.service.defaultimpl;

import org.mltds.sargeras.spi.service.Service;
import org.mltds.sargeras.spi.service.ServiceFactory;

/**
 * @author sunyi
 */
public class DefaultServiceFactory implements ServiceFactory {

    private Service service;

    @Override
    public Service getObject() {
        if (service != null) {
            return service;
        }
        synchronized (DefaultServiceFactory.class) {
            if (service != null) {
                return service;
            }
            service = new DefaultService();
        }
        return service;
    }

}
