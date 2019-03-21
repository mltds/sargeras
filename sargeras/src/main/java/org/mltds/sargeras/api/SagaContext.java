package org.mltds.sargeras.api;

import java.util.*;

import org.mltds.sargeras.api.model.SagaRecord;
import org.mltds.sargeras.api.model.SagaTxRecord;
import org.mltds.sargeras.api.model.SagaTxRecordParam;
import org.mltds.sargeras.api.model.SagaTxRecordResult;
import org.mltds.sargeras.spi.manager.Manager;

/**
 * {@link SagaContext} 在JVM中的生命周期是线程级别的。<br/>
 * 即当执行一个 {@link Saga} 时，会新建一个 {@link SagaContext}，当线程结束时，这个 {@link SagaContext} 对象会被失效。<br/>
 * 如果这个 {@link Saga} 并没有执行完，当 reload 再次重试时，会根据 {@link Manager} 中的信息重新构建出这个 {@link SagaContext}，但是一个全新的JVM对象。<br/>
 * SagaContext 有个很重要的职责是维护内存数据与存储数据的一致性，这点很重要
 *
 * @author sunyi
 */
public class SagaContext {

    private static final String BIZ_PARAM_KEY = "BIZ_PARAM";
    private static final String BIZ_RESULT_KEY = "BIZ_RESULT";

    private static Manager manager = SagaApplication.getManager();

    private Saga saga;
    private SagaRecord record;
    private List<SagaTxRecord> txRecordList;
    private boolean lock = false;
    private Map<String, Object> infoCache = new HashMap<>();

    private SagaContext() {

    }

    public static SagaContext newContext(Saga saga, String bizId) {

        SagaContext context = new SagaContext();
        context.saga = saga;

        SagaRecord record = new SagaRecord();
        record.setAppName(saga.getAppName());
        record.setBizName(saga.getBizName());
        record.setBizId(bizId);
        record.setStatus(SagaStatus.INIT);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);
        record.setTriggerCount(0);

        Calendar now = Calendar.getInstance();
        record.setNextTriggerTime(now.getTime());
        now.add(Calendar.SECOND, saga.getBizTimeout());

        now = Calendar.getInstance();
        int bizTimeout = saga.getBizTimeout();
        now.add(Calendar.SECOND, bizTimeout);
        record.setExpireTime(now.getTime());

        context.record = record;

        return context;
    }

    public static SagaContext loadContext(String appName, String bizName, String bizId) {
        SagaContext context = new SagaContext();

        SagaRecord record = manager.loadContext(appName, bizName, bizId);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);

        context.record = record;

        context.saga = SagaApplication.getSaga(appName, bizName);

        return context;
    }

    public static SagaContext loadContext(long contextId) {
        SagaContext context = new SagaContext();

        SagaRecord record = manager.loadContext(contextId);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);

        context.record = record;

        context.saga = SagaApplication.getSaga(context.getAppName(), context.getBizName());

        return context;
    }

    public void saveAndLock() {
        long id = manager.saveContextAndLock(record, saga.getLockTimeout());
        lock = true;
        record.setId(id);
    }

    public Saga getSaga() {
        return saga;
    }

    public void saveStatus(SagaStatus status) {
        manager.saveContextStatus(record.getId(), status);
        record.setStatus(status);
    }

    /**
     * 将触发次数+1，并保存到存储中。
     */
    public void incrementTriggerCount() {
        manager.incrementTriggerCount(record.getId());
        record.setTriggerCount(record.getTriggerCount() + 1);
    }

    /**
     * 计算下一次的触发时间，并保存到存储中。
     *
     * @return 下一次的触发时间
     */
    public Date saveNextTriggerTime() {
        Date nextTriggerTime = calculationNextTriggerTime();
        manager.saveNextTriggerTime(record.getId(), nextTriggerTime);
        record.setNextTriggerTime(nextTriggerTime);
        return nextTriggerTime;
    }

    private Date calculationNextTriggerTime() {

        int interval;

        int[] triggerInterval = saga.getTriggerInterval();
        int length = saga.getTriggerInterval().length;
        int triggerCount = record.getTriggerCount();

        if (triggerCount <= 0) {
            interval = triggerInterval[0];
        } else if (triggerCount < length) {
            interval = triggerInterval[triggerCount - 1];
        } else {
            interval = triggerInterval[length - 1];
        }

        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, interval);
        return now.getTime();
    }

    /**
     * 获取锁，非阻塞，独占这个SagaContext
     *
     * @return true 为获取锁成功；false 为失败
     */
    public boolean lock() {
        if (this.lock) {
            return true;
        } else {
            Integer lockTimeout = saga.getLockTimeout();
            boolean lock = manager.lock(record.getId(), record.getTriggerId(), lockTimeout);
            this.lock = lock;
            return lock;
        }
    }

    /**
     * 释放锁
     *
     * @return true 为释放锁成功；false为释放失败，比如持有锁超时了
     */
    public boolean unlock() {
        if (!this.lock) {
            return true;
        } else {
            boolean unlock = manager.unlock(record.getId(), record.getTriggerId());
            if (unlock) {
                this.lock = false;
            }
            return unlock;
        }
    }

    public boolean isFirstExecute() {
        return record.getTriggerCount() <= 1;
    }

    public SagaTxRecord saveCurrentTxAndParam(SagaTxRecord txRecord, List<SagaTxRecordParam> txRecordParamList) {
        txRecord = manager.saveCurrentTxAndParam(txRecord, txRecordParamList);
        record.setCurrentTxRecordId(txRecord.getId());

        if (txRecordList == null) {
            txRecordList = manager.findTxRecordList(getRecordId());
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
        return manager.getTxRecordResult(txRecordId);
    }

    public void saveTxStatus(Long txRecordId, SagaTxStatus status) {
        // TODO
    }

    public SagaTxRecordResult saveTxRecordResult(SagaTxRecordResult recordResult) {
        // TODO
        return null;
    }


    public List<SagaTxRecordParam> getTxRecordParam(Long txRecordId) {
        //TODO
        return null;
    }

    /* record getter start */
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

    public int getTriggerCount() {
        return record.getTriggerCount();
    }

    public Date getNextTriggerTime() {
        return record.getNextTriggerTime();
    }

    public Date getCreateTime() {
        return record.getCreateTime();
    }

    public Date getExpireTime() {
        return record.getExpireTime();
    }


    /* record getter end */

}
