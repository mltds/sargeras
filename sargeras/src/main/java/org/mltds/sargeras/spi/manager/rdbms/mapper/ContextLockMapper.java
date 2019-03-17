package org.mltds.sargeras.spi.manager.rdbms.mapper;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.spi.manager.rdbms.model.ContextLockDO;

/**
 * @author sunyi.
 */
public interface ContextLockMapper {

    int insert(ContextLockDO lockDO);

    int delete(@Param("contextId") Long contextId, @Param("reqId") String reqId);

    ContextLockDO select(@Param("contextId") Long contextId);

}
