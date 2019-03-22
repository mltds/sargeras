package org.mltds.sargeras.spi.manager.rdbms;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.exception.SagaContextLockFailException;
import org.mltds.sargeras.api.model.SagaRecord;
import org.mltds.sargeras.api.model.SagaTxRecord;
import org.mltds.sargeras.api.model.SagaTxRecordParam;
import org.mltds.sargeras.api.model.SagaTxRecordResult;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.manager.rdbms.mapper.*;
import org.mltds.sargeras.spi.manager.rdbms.model.ContextInfoDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi
 */
public class RdbmsManager implements Manager {

    private static final Logger logger = LoggerFactory.getLogger(RdbmsManager.class);

    private SagaRecordMapper sagaRecordMapper;
    private SagaTxRecordMapper sagaTxRecordMapper;
    private SagaTxRecordParamMapper sagaTxRecordParamMapper;
    private SagaTxRecordResultMapper sagaTxRecordResultMapper;

    private ContextInfoMapper contextInfoMapper;
    // private ContextLockMapper contextLockMapper;

    private JsonSerialize serialize = new JsonSerialize();

    @Override
    @Transaction
    public long saveContextAndLock(SagaRecord sagaRecord, int lockTimeout) {

        Calendar c = Calendar.getInstance();

        sagaRecord.setCreateTime(c.getTime());
        sagaRecord.setModifyTime(c.getTime());

        sagaRecordMapper.insert(sagaRecord);

        Long id = sagaRecord.getId();

        boolean lock = lock(id, sagaRecord.getTriggerId(), lockTimeout);
        if (!lock) {
            throw new SagaContextLockFailException(sagaRecord.getId(), sagaRecord.getTriggerId());
        }

        return id;
    }

    @Override
    public SagaRecord loadContext(long contextId) {
        return sagaRecordMapper.selectById(contextId);
    }

    @Override
    public SagaRecord loadContext(String appName, String bizName, String bizId) {
        return sagaRecordMapper.selectByBiz(appName, bizName, bizId);
    }

    @Override
    public void saveRecordStatus(long contextId, SagaStatus status) {
        SagaRecord sagaRecord = new SagaRecord();
        sagaRecord.setId(contextId);
        sagaRecord.setStatus(status);
        sagaRecord.setModifyTime(new Date());
        sagaRecordMapper.updateById(sagaRecord);
    }

    @Override
    @Transaction
    public long saveTxRecordAndParam(SagaTxRecord txRecord, List<SagaTxRecordParam> paramList) {

        sagaTxRecordMapper.insert(txRecord);

        sagaTxRecordParamMapper.insertList(paramList);

        return txRecord.getId();

    }

    @Override
    public void incrementTriggerCount(long contextId) {
        sagaRecordMapper.incrementTriggerCount(contextId, new Date());
    }

    @Override
    public void saveNextTriggerTime(long contextId, Date nextTriggerTime) {
        SagaRecord sagaRecord = new SagaRecord();
        sagaRecord.setId(contextId);
        sagaRecord.setNextTriggerTime(nextTriggerTime);
        sagaRecord.setModifyTime(new Date());
        sagaRecordMapper.updateById(sagaRecord);
    }

    @Override
    public void saveContextInfo(long contextId, String key, Object info) {
        ContextInfoDO contextInfoDO = contextInfoMapper.selectByKey(contextId, key);
        if (contextInfoDO == null) {
            contextInfoDO = new ContextInfoDO();
            contextInfoDO.setContextId(contextId);
            contextInfoDO.setKey(key);
            String infoStr = serialize.write(info);
            contextInfoDO.setInfo(infoStr);
            Date now = new Date();
            contextInfoDO.setCreateTime(now);
            contextInfoDO.setModifyTime(now);

            contextInfoMapper.insert(contextInfoDO);
        } else {

            String infoStr = serialize.write(info);
            contextInfoDO.setKey(infoStr);
            contextInfoDO.setModifyTime(new Date());

            contextInfoMapper.updateById(contextInfoDO);
        }
    }

    @Override
    public <T> T loadContextInfo(long contextId, String key, Class<T> cls) {

        ContextInfoDO contextInfoDO = contextInfoMapper.selectByKey(contextId, key);
        if (contextInfoDO == null || contextInfoDO.getInfo() == null) {
            return null;
        }
        return serialize.read(contextInfoDO.getInfo(), cls);
    }

    @Override
    public boolean lock(long id, String triggerId, int lockExpireSec) {

        try {

            SagaRecord sagaRecord = sagaRecordMapper.selectById(id);

            if (sagaRecord == null) {
                return false;
            } else if (!sagaRecord.isLocked() || triggerId.equals(sagaRecord.getTriggerId())) {
                Date date = calExpireTime(lockExpireSec);
                int update = sagaRecordMapper.updateForLock(id, triggerId, date);
                return update > 0;
            } else {
                Calendar c = Calendar.getInstance();
                boolean after = c.getTime().after(sagaRecord.getExpireTime());
                if (after) {
                    Date date = calExpireTime(lockExpireSec);
                    int lock = sagaRecordMapper.updateForLock(id, sagaRecord.getTriggerId(), triggerId, date);
                    return lock > 0;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    // private ContextLockDO newLock(long id, String triggerId, int timeoutSec) {
    // ContextLockDO contextLockDO = new ContextLockDO();
    // contextLockDO.setContextId(id);
    // contextLockDO.setTriggerId(triggerId);
    //
    // Calendar c = Calendar.getInstance();
    // contextLockDO.setCreateTime(c.getTime());
    //
    // Date date = calExpireTime(timeoutSec);
    // contextLockDO.setExpireTime(date);
    //
    // return contextLockDO;
    // }

    private Date calExpireTime(int timeout) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, timeout);
        return c.getTime();
    }

    @Override
    public boolean unlock(long id, String triggerId) {

        try {
            SagaRecord sagaRecord = sagaRecordMapper.selectById(id);
            if (sagaRecord == null || !sagaRecord.getTriggerId().equals(triggerId)) {
                return false;
            } else {
                int count = sagaRecordMapper.updateForUnlock(id, triggerId);
                return count > 0;
            }
        } catch (Exception e) {
            logger.warn("操作数据库释放锁失败ContextId:{" + id + "},triggerId:{" + triggerId + "}", e);
            return false;
        }
    }

    @Override
    public List<Long> findNeedRetryContextList(Date beforeTriggerTime, int limit) {
        return sagaRecordMapper.findNeedRetryRecordList(beforeTriggerTime, limit);
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

    // private ContextDO sagaContextToContextDO(SagaRecord sagaRecord) {
    // try {
    // ContextDO contextDO = new ContextDO();
    // BeanUtils.copyProperties(contextDO, sagaRecord);
    // return contextDO;
    // } catch (Exception e) {
    // throw new SagaException("SagaContextBase转换为ContextDO失败，BizID:" + sagaRecord.getBizId(), e);
    // }
    // }

    // private SagaRecord contextDOToSagaContext(ContextDO contextDO) throws ClassNotFoundException {
    // try {
    // SagaRecord sagaRecord = new SagaRecord();
    // BeanUtils.copyProperties(sagaRecord, contextDO);
    // return sagaRecord;
    // } catch (Exception e) {
    // throw new SagaException("ContextDO转换为SagaContextBase失败，BizID:" + contextDO.getBizId(), e);
    // }
    // }

    void setSagaRecordMapper(SagaRecordMapper sagaRecordMapper) {
        this.sagaRecordMapper = sagaRecordMapper;
    }

    void setContextInfoMapper(ContextInfoMapper contextInfoMapper) {
        this.contextInfoMapper = contextInfoMapper;
    }
    // void setContextLockMapper(ContextLockMapper contextLockMapper) {
    // this.contextLockMapper = contextLockMapper;
    // }

}
