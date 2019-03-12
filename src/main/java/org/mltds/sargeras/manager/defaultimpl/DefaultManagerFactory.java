package org.mltds.sargeras.manager.defaultimpl;

import org.mltds.sargeras.manager.Manager;
import org.mltds.sargeras.manager.ManagerFactory;

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

            manager = new Manager();

        }

        return manager;
    }

}
