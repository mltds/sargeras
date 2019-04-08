package org.mltds.sargeras.spi.manager;

import java.util.Date;
import java.util.List;

import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.model.*;

/**
 * @author sunyi
 */
public interface Manager {

    /**
     * 作为一条新记录去创建并获取锁，如果创建成功则获取锁也要保证一定成功，如果 BizId 已存在则报错
     */
    long firstTrigger(SagaRecord record, List<SagaRecordParam> recordParamList);

    /**
     * 尝试获取锁，获取锁成功触发次数+1，设施锁过期时间和下一次触发时间。如果获取锁失败则放弃。
     */
    boolean trigger(Long recordId, String triggerId, Date nextTriggerTime, Date lockExpireTime);

    boolean triggerOver(Long recordId, String triggerId);

    /**
     * 根据 RecordId 从存储的信息中，重新构建出一个 {@link SagaRecord}
     */
    SagaRecord findRecord(long recordId);

    /**
     * 根据 BizId 从存储的信息中，重新构建出一个 {@link SagaRecord}
     */
    SagaRecord findRecord(String appName, String bizName, String bizId);

    /**
     * 保存当前要执行/执行中的TX
     */
    long saveTxRecordAndParam(SagaTxRecord txRecord, List<SagaTxRecordParam> paramList);

    /**
     * 保存状态并更新ModifyTime
     */
    void saveRecordStatus(long recordId, SagaStatus status);

    /**
     * 保存状态并更新ModifyTime
     */
    void saveRecordStatusAndResult(long recordId, SagaStatus status, SagaRecordResult recordResult);

    /**
     * 查询出需要轮询重试的 Record Id List
     * 
     * @param limit 返回最多条数，参见数据库的 limit。
     * @return Record ID 的集合
     */
    List<Long> findNeedRetryRecordList(Date beforeTriggerTime, int limit);

    /**
     * 如果无数据，需要返回空的List，而不能是 null
     */
    List<SagaTxRecord> findTxRecordList(Long recordId);

    SagaTxRecordResult findTxRecordResult(Long txRecordId);

    void saveTxRecordStatus(Long txRecordId, SagaTxStatus status);

    /**
     * 将 TxRecord 标记为成功，并保存执行结果
     */
    void saveTxRecordSuccAndResult(SagaTxRecordResult recordResult);

    List<SagaTxRecordParam> findTxRecordParam(Long txRecordId);

    List<SagaRecordParam> findRecordParam(long recordId);
}