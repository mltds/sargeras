package org.mltds.sargeras.spi.manager.rdbms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.api.model.SagaTxRecord;

/**
 * @author sunyi.
 */
public interface SagaTxRecordMapper {

    int insert(SagaTxRecord txRecord);

    int updateById(SagaTxRecord txRecord);

    List<SagaTxRecord> selectByRecordId(@Param("recordId") Long recordId);
}
