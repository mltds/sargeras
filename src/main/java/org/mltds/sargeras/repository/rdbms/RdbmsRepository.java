package org.mltds.sargeras.repository.rdbms;

import org.mltds.sargeras.SagaContext;
import org.mltds.sargeras.SagaStatus;
import org.mltds.sargeras.repository.Repository;

/**
 * @author sunyi 2019/2/20.
 */
public class RdbmsRepository implements Repository {

    @Override
    public SagaContext saveContext(SagaContext context) {
        return null;
    }

    @Override
    public SagaContext findContext(Long id) {
        return null;
    }

    @Override
    public void updateContextById(SagaContext context) {

    }

    @Override
    public boolean lock(Long id) {
        return false;
    }

    @Override
    public boolean unlock(Long id) {
        return false;
    }

}
