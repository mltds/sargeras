package org.mltds.sargeras.spi.manager.rdbms.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.spi.manager.rdbms.model.ContextLockDO;

/**
 * @author sunyi.
 */
public interface ContextLockMapper {

    int insert(ContextLockDO lockDO);

    int update(@Param("contextId") Long contextId, @Param("triggerId") String triggerId, @Param("expireTime") Date expireTime);

    int delete(@Param("contextId") Long contextId, @Param("triggerId") String triggerId);

    ContextLockDO select(@Param("contextId") Long contextId);

}