package org.mltds.sargeras.worker.spi.manager.defaultimpl;

import org.mltds.sargeras.worker.spi.manager.Manager;
import org.mltds.sargeras.worker.spi.manager.ManagerFactory;

/**
 * @author sunyi
 */
public class DefaultManagerFactory implements ManagerFactory {

    private Manager manager;

    @Override
    public Manager getObject() {
        if (manager != null) {
            return manager;
        }
        synchronized (DefaultManagerFactory.class) {
            if (manager != null) {
                return manager;
            }
            manager = new DefaultManager();
        }
        return manager;
    }

}
