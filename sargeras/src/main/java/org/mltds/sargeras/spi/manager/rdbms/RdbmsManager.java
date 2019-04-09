package org.mltds.sargeras.spi.manager.rdbms;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.model.*;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.manager.rdbms.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sunyi
 */
@Component
public class RdbmsManager implements Manager {

    @Autowired
    private SagaRecordMapper sagaRecordMapper;

    @Autowired
    private SagaRecordParamMapper sagaRecordParamMapper;

    @Autowired
    private SagaRecordResultMapper recordResultMapper;

    @Autowired
    private SagaTxRecordMapper sagaTxRecordMapper;

    @Autowired
    private SagaTxRecordParamMapper sagaTxRecordParamMapper;

    @Autowired
    private SagaTxRecordResultMapper sagaTxRecordResultMapper;

    @Override
    @Transactional(rollbackFor = Throwable.class)
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
    @Transactional(rollbackFor = Throwable.class)
    public boolean trigger(Long recordId, String triggerId, Date nextTriggerTime, Date lockExpireTime) {

        boolean locked;
        SagaRecord sagaRecord = sagaRecordMapper.selectById(recordId);

        if (sagaRecord == null) {
            return false;
        } else if (!sagaRecord.isLocked()) {
            // 没有被锁，尝试获取锁
            int update = sagaRecordMapper.updateForLock(recordId, null, triggerId, lockExpireTime);
            locked = update > 0;
        } else if (sagaRecord.isLocked() && triggerId.equals(sagaRecord.getTriggerId())) {
            // 已经被锁，但是是被自己这个TriggerId锁住的，那么去尝试获取锁，并刷新锁的过期时间。
            int update = sagaRecordMapper.updateForLock(recordId, triggerId, triggerId, lockExpireTime);
            locked = update > 0;
        } else {
            // 已经被锁，且被其他 Trigger ID 锁住的
            Calendar c = Calendar.getInstance();
            boolean after = c.getTime().after(sagaRecord.getLockExpireTime());
            if (after) {
                // 当前时间晚于锁的过期时间，尝试获取锁
                int lock = sagaRecordMapper.updateForLock(recordId, sagaRecord.getTriggerId(), triggerId, lockExpireTime);
                locked = lock > 0;
            } else {
                // 当前时间早于锁的过期时间，放弃获取锁，返回失败
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
    @Transactional(rollbackFor = Throwable.class)
    public void saveRecordStatusAndResult(long recordId, SagaStatus status, SagaRecordResult recordResult) {
        Date now = new Date();
        recordResult.setCreateTime(now);
        recordResult.setModifyTime(now);

        recordResultMapper.insert(recordResult);

        sagaRecordMapper.updateStatus(recordId, status, new Date());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public long saveTxRecordAndParam(SagaTxRecord txRecord, List<SagaTxRecordParam> paramList) {

        Date now = new Date();
        txRecord.setCreateTime(now);
        txRecord.setModifyTime(now);
        sagaTxRecordMapper.insert(txRecord);

        Long id = txRecord.getId();

        if (CollectionUtils.isNotEmpty(paramList)) {
            for (SagaTxRecordParam param : paramList) {
                param.setTxRecordId(id);
                param.setCreateTime(now);
                param.setModifyTime(now);
            }

            sagaTxRecordParamMapper.insertList(paramList);
        }

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
    public SagaTxRecordResult findTxRecordResult(Long txRecordId) {
        return sagaTxRecordResultMapper.selectByTxRecordId(txRecordId);
    }

    @Override
    public void saveTxRecordStatus(Long txRecordId, SagaTxStatus status) {
        SagaTxRecord txRecord = new SagaTxRecord();
        txRecord.setId(txRecordId);
        txRecord.setStatus(status);
        txRecord.setModifyTime(new Date());
        sagaTxRecordMapper.updateById(txRecord);
    }

    @Override
    public void saveTxRecordSuccAndResult(SagaTxRecordResult recordResult) {

        Long txRecordId = recordResult.getTxRecordId();
        saveTxRecordStatus(txRecordId, SagaTxStatus.SUCCESS);

        Date now = new Date();
        recordResult.setCreateTime(now);
        recordResult.setModifyTime(now);
        sagaTxRecordResultMapper.insert(recordResult);

    }

    @Override
    public List<SagaTxRecordParam> findTxRecordParam(Long txRecordId) {
        return sagaTxRecordParamMapper.selectByTxRecordId(txRecordId);
    }

    @Override
    public List<SagaRecordParam> findRecordParam(long recordId) {
        return sagaRecordParamMapper.selectByRecordId(recordId);
    }

}
