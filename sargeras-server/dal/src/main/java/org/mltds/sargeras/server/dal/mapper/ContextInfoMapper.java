package org.mltds.sargeras.server.dal.mapper;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.server.dal.model.ContextInfoDO;

/**
 * @author sunyi.
 */
public interface ContextInfoMapper {

    int insert(ContextInfoDO info);

    int updateById(ContextInfoDO info);

    int updateByKey(ContextInfoDO info);

    ContextInfoDO selectByKey(@Param("contextId") Long contextId, @Param("key") String key);

}
