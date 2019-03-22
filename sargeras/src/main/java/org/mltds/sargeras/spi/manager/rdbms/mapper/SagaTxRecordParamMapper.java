package org.mltds.sargeras.spi.manager.rdbms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.api.model.SagaTxRecordParam;

/**
 * @author sunyi.
 */
public interface SagaTxRecordParamMapper {

    int insertList(List<SagaTxRecordParam> paramList);

    List<SagaTxRecordParam> selectByTxRecordId(@Param("txRecordId") Long txRecordId);

}
