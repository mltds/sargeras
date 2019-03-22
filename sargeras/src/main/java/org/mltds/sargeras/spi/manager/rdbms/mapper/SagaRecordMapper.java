package org.mltds.sargeras.spi.manager.rdbms.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.api.model.SagaRecord;

/**
 * @author sunyi
 */
public interface SagaRecordMapper {

    int insert(SagaRecord sagaRecord);

    SagaRecord selectById(Long id);

    SagaRecord selectByBiz(String appName, String bizName, String bizId);

    /**
     * 仅更新有限的字段(status,pre_executed_tx，pre_compensated_tx,modify_time)通用方法
     */
    int updateById(SagaRecord sagaRecord);

    int incrementTriggerCount(@Param("id") Long id, @Param("modifyTime") Date modifyTime);


    int updateForLock(@Param("id") Long id, @Param("triggerId") String triggerId, @Param("expireTime") Date expireTime);

    int updateForLock(@Param("id") Long id, @Param("oldTriggerId") String oldTriggerId, @Param("newTriggerId") String newTriggerId, @Param("expireTime") Date expireTime);

    int updateForUnlock(@Param("id") Long id, @Param("triggerId") String triggerId);

    List<Long> findNeedRetryRecordList(@Param("beforeTriggerTime") Date beforeTriggerTime, @Param("limit") int limit);

}
