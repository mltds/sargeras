package org.mltds.sargeras.core;

import java.util.*;

import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.SagaLockFailException;
import org.mltds.sargeras.api.listener.*;
import org.mltds.sargeras.api.model.*;
import org.mltds.sargeras.spi.manager.Manager;

/**
 * {@link SagaContext} 在JVM中的生命周期是线程级别的。即当执行一个 {@link Saga} 时，会新建一个 {@link SagaContext}，当线程结束时，这个 {@link SagaContext} 对象会被失效。如果这个 {@link Saga} 并没有执行完，当 reload
 * 再次重试时，会根据 {@link Manager} 中的信息重新构建出这个 {@link SagaContext}，但是一个全新的JVM对象。<br/>
 * 同时 SagaContext 也缓存了一些执行过程中需要的信息，那么也要维护内存数据与存储数据的一致性，这点很重要
 *
 * @author sunyi
 */
public class SagaContext {

    Saga saga;
    SagaRecord record;
    List<SagaTxRecord> txRecordList;

    Manager manager;
    SagaListenerChain sagaListenerChain;
    SagaTxListenerChain sagaTxListenerChain;

    SagaContext() {

    }

    public void firstTrigger(String bizId, MethodInfo methodInfo, List<ParamInfo> paramInfoList) {
        SagaRecord record = new SagaRecord();
        record.setAppName(saga.getAppName());
        record.setBizName(saga.getBizName());
        record.setBizId(bizId);

        record.setCls(methodInfo.getClsName());
        record.setMethod(methodInfo.getMethodName());
        record.setParameterTypes(methodInfo.getParameterTypesStr());

        record.setStatus(SagaStatus.EXECUTING);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);
        record.setTriggerCount(1);

        Date date = calculationNextTriggerTime(1);// 首次执行，默认为1
        record.setNextTriggerTime(date);

        record.setLocked(true);

        Calendar c = Calendar.getInstance();
        int lockTimeout = saga.getLockTimeout();
        c.add(Calendar.SECOND, lockTimeout);
        record.setLockExpireTime(c.getTime());

        c = Calendar.getInstance();
        int bizTimeout = saga.getBizTimeout();
        c.add(Calendar.SECOND, bizTimeout);
        record.setExpireTime(c.getTime());

        this.record = record;

        List<SagaRecordParam> recordParamList = new ArrayList<>(paramInfoList.size());
        for (ParamInfo paramInfo : paramInfoList) {
            SagaRecordParam recordParam = new SagaRecordParam();
            recordParam.setParameterType(paramInfo.getParameterTypeStr());
            recordParam.setParameterName(paramInfo.getParameterName());
            recordParam.setParameter(paramInfo.getParameterByte());
            recordParamList.add(recordParam);
        }

        long id = manager.firstTrigger(record, recordParamList);

        this.record.setId(id);

        notifySagaListener(SagaStatus.EXECUTING, record);

    }

    /**
     * 尝试触发一次并获取锁，如果获取锁成功，则触发次数+1，修改下一次触发时间，锁的过期时间。
     *
     * @throws SagaLockFailException 如果获取锁失败则抛出异常
     */
    public void trigger() throws SagaLockFailException {

        Long recordId = record.getId();
        String triggerId = record.getTriggerId();
        int triggerCount = record.getTriggerCount() + 1;

        Date nextTriggerTime = calculationNextTriggerTime(triggerCount);

        Calendar c = Calendar.getInstance();
        int lockTimeout = saga.getLockTimeout();
        c.add(Calendar.SECOND, lockTimeout);
        Date lockExpireTime = c.getTime();

        boolean locked = manager.trigger(recordId, triggerId, nextTriggerTime, lockExpireTime);

        if (locked) {
            record.setTriggerCount(triggerCount);
            record.setNextTriggerTime(nextTriggerTime);
            record.setLockExpireTime(lockExpireTime);
        } else {
            throw new SagaLockFailException(record.getId(), record.getTriggerId());
        }

        notifySagaListener(SagaStatus.EXECUTING, record);
    }

    public void triggerOver() {
        manager.triggerOver(record.getId(), record.getTriggerId());
    }

    public Saga getSaga() {
        return saga;
    }

    public void saveStatus(SagaStatus status) {
        manager.saveRecordStatus(record.getId(), status);
        record.setStatus(status);

        notifySagaListener(status, record);

    }

    public void saveStatusAndResult(SagaStatus status, SagaRecordResult recordResult) {
        manager.saveRecordStatusAndResult(record.getId(), status, recordResult);
        record.setStatus(status);

        notifySagaListener(status, record);
    }

    /**
     * @param currentTriggerCount 本次触发次数，因为不确定中为上次触发次数，所以这里不从 Record 里获取，改为外部传入的方式，需为大于0的数字
     */
    private Date calculationNextTriggerTime(int currentTriggerCount) {

        int interval;

        int[] triggerInterval = saga.getTriggerInterval();
        int length = saga.getTriggerInterval().length;

        if (currentTriggerCount <= 0) {
            throw new SagaException("错误的currentTriggerCount：" + currentTriggerCount);
        } else if (currentTriggerCount < length) {
            interval = triggerInterval[currentTriggerCount - 1];
        } else {
            interval = triggerInterval[length - 1];
        }

        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, interval);
        return now.getTime();
    }

    public List<SagaRecordParam> getRecordParam(long recordId) {
        return manager.findRecordParam(recordId);
    }

    public boolean isFirstTrigger() {
        return record.getTriggerCount() <= 1;
    }

    public SagaTxRecord saveCurrentTxAndParam(SagaTxRecord txRecord, List<SagaTxRecordParam> txRecordParamList) {
        Long txRecordId = manager.saveTxRecordAndParam(txRecord, txRecordParamList);
        txRecord.setId(txRecordId);

        if (txRecordList == null) {
            txRecordList = manager.findTxRecordList(getRecordId());
            if (txRecordList == null) {
                txRecordList = new ArrayList<>();
            }
        }
        txRecordList.add(txRecord);

        return txRecord;
    }

    public List<SagaTxRecord> getTxRecordList() {
        if (txRecordList == null) {
            txRecordList = manager.findTxRecordList(getRecordId());
        }

        return Collections.unmodifiableList(txRecordList);
    }

    public SagaTxRecordResult getTxRecordResult(Long txRecordId) {
        return manager.findTxRecordResult(txRecordId);
    }

    public void saveTxStatus(Long txRecordId, SagaTxStatus status) {

        manager.saveTxRecordStatus(txRecordId, status);

        for (SagaTxRecord record : txRecordList) {
            if (record.getId().equals(txRecordId)) {
                record.setStatus(status);
                notifySagaTxListener(status, record);
                return;
            }
        }

    }

    public void saveTxRecordSuccAndResult(SagaTxRecordResult recordResult) {
        manager.saveTxRecordSuccAndResult(recordResult);

        for (SagaTxRecord record : txRecordList) {
            if (record.getId().equals(recordResult.getTxRecordId())) {
                record.setStatus(SagaTxStatus.SUCCESS);
                notifySagaTxListener(SagaTxStatus.SUCCESS, record);
                return;
            }
        }
    }

    public List<SagaTxRecordParam> getTxRecordParam(Long txRecordId) {
        return manager.findTxRecordParam(txRecordId);
    }

    private void notifySagaListener(SagaStatus status, SagaRecord record) {
        switch (status) {
        case EXECUTING:
            sagaListenerChain.event(new SagaEvent(SagaEventType.ON_TRIGGER, record));
            break;
        case EXECUTE_SUCC:
            sagaListenerChain.event(new SagaEvent(SagaEventType.ON_EXECUTE_SUCCESS, record));
            break;
        case COMPENSATING:
            sagaListenerChain.event(new SagaEvent(SagaEventType.ON_COMPENSATE_FAILURE, record));
            break;
        case COMPENSATE_SUCC:
            sagaListenerChain.event(new SagaEvent(SagaEventType.ON_COMPENSATE_SUCCESS, record));
            break;
        case COMPENSATE_FAIL:
            sagaListenerChain.event(new SagaEvent(SagaEventType.ON_COMPENSATE_FAILURE, record));
            break;
        }
    }

    private void notifySagaTxListener(SagaTxStatus status, SagaTxRecord record) {
        switch (status) {
        case SUCCESS:
            sagaTxListenerChain.event(new SagaTxEvent(SagaTxEventType.ON_EXECUTE_SUCCESS, record));
            break;
        case PROCESSING:
            sagaTxListenerChain.event(new SagaTxEvent(SagaTxEventType.ON_COMPENSATE_PROCESS, record));
            break;
        case FAILURE:
            sagaTxListenerChain.event(new SagaTxEvent(SagaTxEventType.ON_COMPENSATE_FAILURE, record));
            break;
        case COMPENSATE_SUCCESS:
            sagaTxListenerChain.event(new SagaTxEvent(SagaTxEventType.ON_COMPENSATE_SUCCESS, record));
            break;
        case COMPENSATE_PROCESSING:
            sagaTxListenerChain.event(new SagaTxEvent(SagaTxEventType.ON_COMPENSATE_PROCESS, record));
            break;
        case COMPENSATE_FAILURE:
            sagaTxListenerChain.event(new SagaTxEvent(SagaTxEventType.ON_COMPENSATE_FAILURE, record));
            break;

        }
    }

    /* getter start */
    public Long getRecordId() {
        return record.getId();
    }

    public String getAppName() {
        return record.getAppName();
    }

    public String getBizName() {
        return record.getBizName();
    }

    public String getBizId() {
        return record.getBizId();
    }

    public SagaStatus getStatus() {
        return record.getStatus();
    }

    public String getTriggerId() {
        return record.getTriggerId();
    }

    public Date getNextTriggerTime() {
        return record.getNextTriggerTime();
    }

    public Date getExpireTime() {
        return record.getExpireTime();
    }

    /* getter end */

}
