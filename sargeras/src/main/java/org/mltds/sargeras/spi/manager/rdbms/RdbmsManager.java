package org.mltds.sargeras.spi.manager.rdbms;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.model.*;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.manager.rdbms.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author sunyi
 */
public class RdbmsManager implements Manager {

    private static final Logger logger = LoggerFactory.getLogger(RdbmsManager.class);

    @Autowired
    private SagaRecordMapper sagaRecordMapper;

    @Autowired
    private SagaRecordParamMapper sagaRecordParamMapper;

    @Autowired
    private SagaTxRecordMapper sagaTxRecordMapper;

    @Autowired
    private SagaTxRecordParamMapper sagaTxRecordParamMapper;

    @Autowired
    private SagaTxRecordResultMapper sagaTxRecordResultMapper;

    @Override
    @Transaction
    public long firstTrigger(SagaRecord record, List<SagaRecordParam> recordParamList) {

        Date now = new Date();
        record.setCreateTime(now);
        record.setModifyTime(now);
        sagaRecordMapper.insert(record);
        Long id = record.getId();

        for (SagaRecordParam recordParam : recordParamList) {
            recordParam.setRecordId(id);
            recordParam.setCreateTime(now);
            recordParam.setModifyTime(now);
        }

        sagaRecordParamMapper.insertList(recordParamList);

        return id;
    }

    @Override
    @Transaction
    public boolean trigger(Long recordId, String triggerId, Date nextTriggerTime, Date lockExpireTime) {

        boolean locked;
        SagaRecord sagaRecord = sagaRecordMapper.selectById(recordId);

        if (sagaRecord == null) {
            return false;
        } else if (!sagaRecord.isLocked() || triggerId.equals(sagaRecord.getTriggerId())) {
            int update = sagaRecordMapper.updateForLock(recordId, null, triggerId, lockExpireTime);
            locked = update > 0;
        } else {
            Calendar c = Calendar.getInstance();
            boolean after = c.getTime().after(sagaRecord.getExpireTime());
            if (after) {
                int lock = sagaRecordMapper.updateForLock(recordId, sagaRecord.getTriggerId(), triggerId, lockExpireTime);
                locked = lock > 0;
            } else {
                return false;
            }
        }

        if (locked) {
            sagaRecordMapper.updateNextTriggerTimeAndIncrementCount(recordId, nextTriggerTime, new Date());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean triggerOver(Long recordId, String triggerId) {
        SagaRecord sagaRecord = sagaRecordMapper.selectById(recordId);
        if (sagaRecord == null || !sagaRecord.getTriggerId().equals(triggerId)) {
            return false;
        } else {
            int count = sagaRecordMapper.updateForUnlock(recordId, triggerId);
            return count > 0;
        }
    }

    @Override
    public SagaRecord findRecord(long contextId) {
        return sagaRecordMapper.selectById(contextId);
    }

    @Override
    public SagaRecord findRecord(String appName, String bizName, String bizId) {
        return sagaRecordMapper.selectByBiz(appName, bizName, bizId);
    }

    @Override
    public void saveRecordStatus(long recordId, SagaStatus status) {
        sagaRecordMapper.updateStatus(recordId, status, new Date());
    }

    @Override
    @Transaction
    public long saveTxRecordAndParam(SagaTxRecord txRecord, List<SagaTxRecordParam> paramList) {

        sagaTxRecordMapper.insert(txRecord);

        sagaTxRecordParamMapper.insertList(paramList);

        return txRecord.getId();

    }

    @Override
    public List<Long> findNeedRetryRecordList(Date beforeTriggerTime, int limit) {
        return sagaRecordMapper.selectNeedRetryRecordList(beforeTriggerTime, limit);
    }

    @Override
    public List<SagaTxRecord> findTxRecordList(Long recordId) {
        return sagaTxRecordMapper.selectByRecordId(recordId);
    }

    @Override
    public SagaTxRecordResult getTxRecordResult(Long txRecordId) {
        return sagaTxRecordResultMapper.selectByTxRecordId(txRecordId);
    }

    @Override
    public void saveTxRecordStatus(Long txRecordId, SagaTxStatus status) {
        SagaTxRecord txRecord = new SagaTxRecord();
        txRecord.setId(txRecordId);
        txRecord.setStatus(status);
        sagaTxRecordMapper.updateById(txRecord);
    }

    @Override
    @Transaction
    public void saveTxRecordSuccAndResult(SagaTxRecordResult recordResult) {

        Long txRecordId = recordResult.getTxRecordId();
        saveTxRecordStatus(txRecordId, SagaTxStatus.SUCCESS);

        sagaTxRecordResultMapper.insert(recordResult);

    }

    @Override
    public List<SagaTxRecordParam> getTxRecordParam(Long txRecordId) {
        return sagaTxRecordParamMapper.selectByTxRecordId(txRecordId);
    }

    @Override
    public List<SagaRecordParam> findRecordParam(long recordId) {
        return sagaRecordParamMapper.selectByRecordId(recordId);
    }

}
