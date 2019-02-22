package org.mltds.sargeras.repository;

import org.mltds.sargeras.SagaContext;

/**
 * @author sunyi 2019/2/20.
 */
public interface Repository {

    SagaContext saveContext(SagaContext context);

    SagaContext findContext(Long id);

    void updateContextById(SagaContext context);

    boolean lock(Long id);

    boolean unlock(Long id);

}
