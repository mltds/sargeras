package org.mltds.sargeras.spi.manager.rdbms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.api.model.SagaRecordParam;

/**
 * @author sunyi.
 */
public interface SagaRecordParamMapper {

    int insertList(List<SagaRecordParam> paramList);

    List<SagaRecordParam> selectByRecordId(@Param("recordId") Long recordId);

}
