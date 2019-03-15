package org.mltds.sargeras.server.dal.mapper;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.server.dal.model.ContextLockDO;

/**
 * @author sunyi.
 */
public interface ContextLockMapper {

    int insert(ContextLockDO lockDO);

    int delete(@Param("contextId") Long contextId, @Param("reqId") String reqId);

    ContextLockDO select(@Param("contextId") Long contextId);

}
