package org.mltds.sargeras.spi.manager.rdbms.mapper;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.api.model.SagaRecordResult;

/**
 * @author sunyi.
 */
public interface SagaRecordResultMapper {

    int insert(SagaRecordResult recordResult);

    SagaRecordResult selectByRecordId(@Param("recordId") Long recordId);

}
