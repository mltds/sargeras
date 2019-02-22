package org.mltds.sargeras.repository.rdbms.mapper;

import org.mltds.sargeras.SagaContext;

/**
 * @author sunyi 2019/2/20.
 */
public interface ContextMapper {

    SagaContext selectById(Long id);

}
