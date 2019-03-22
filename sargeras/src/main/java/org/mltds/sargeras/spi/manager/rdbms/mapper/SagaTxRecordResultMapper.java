package org.mltds.sargeras.spi.manager.rdbms.mapper;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.api.model.SagaTxRecordResult;

/**
 * @author sunyi.
 */
public interface SagaTxRecordResultMapper {

    int insert(SagaTxRecordResult recordResult);

    SagaTxRecordResult selectByTxRecordId(@Param("txRecordId") Long txRecordId);

}
