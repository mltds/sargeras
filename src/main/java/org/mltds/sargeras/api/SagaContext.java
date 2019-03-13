package org.mltds.sargeras.api;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mltds.sargeras.repository.Repository;

/**
 * {@link SagaContext} 在JVM中的生命周期是线程级别的。<br/>
 * 即当执行一个 {@link Saga} 时，会新建一个 {@link SagaContext}，当线程结束时，这个 {@link SagaContext} 对象会被失效。<br/>
 * 如果这个 {@link Saga} 并没有执行完，当 reload 再次重试时，会根据 {@link Repository} 中的信息重新构建出这个 {@link SagaContext}，但是一个全新的JVM对象。<br/>
 * 
 * @author sunyi
 */
public class SagaContext {

    private static final String BIZ_PARAM_KEY = "BIZ_PARAM";
    private static final String BIZ_RESULT_KEY = "BIZ_RESULT";

    private Saga saga;

    private Long id;

    private String bizId;
    private Object bizParam;
    private Object bizResult;

    private SagaStatus status;

    private Class<? extends SagaTx> currentTx;
    private Class<? extends SagaTx> preExecutedTx;
    private Class<? extends SagaTx> preCompensatedTx;

    /**
     * 每次执行的id, 伴随着 Context 对象的生命周期，不需要存储<br/>
     * 比如第一次执行，triggerId 为 A ，返回处理中后挂起，过段时间第二次执行的时候为 B 。<br/>
     */
    private transient String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
    /**
     * 触发执行次数，首次执行记为1，每次轮询重试+1
     */
    private int triggerCount;
    /**
     * 当遇到处理中的情况时，期望下一次轮询重试的触发时间。
     */
    private Date nextTriggerTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间，过期时间，创建时间加业务超时时间
     */
    private Date expireTime;

    /**
     * 最新修改时间
     */
    private Date modifyTime;

    private Map<String, Object> transientInfo = new HashMap<>();

    private AtomicBoolean locked = new AtomicBoolean(false);

    private Repository repository = SagaApplication.getRepository();

    public SagaContext(Saga saga) {
        this.saga = saga;
    }

    public Saga getSaga() {
        return saga;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public void saveStatus(SagaStatus status) {
        setStatus(status);
        repository.saveContextStatus(this.id, status);
    }

    public String getBizId() {
        return this.bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBizParam(Class<T> t) {
        if (bizParam != null) {
            return (T) bizParam;
        }
        return getPersistentInfo(BIZ_PARAM_KEY, t);
    }

    public void setBizParam(Object bizParam) {
        this.bizParam = bizParam;
    }

    public void saveBizParam(Object bizParam) {
        setBizParam(bizParam);
        savePersistentInfo(BIZ_PARAM_KEY, bizParam);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBizResult(Class<T> t) {
        if (bizResult != null) {
            return (T) bizResult;
        }
        return getPersistentInfo(BIZ_RESULT_KEY, t);
    }

    public void setBizResult(Object bizResult) {
        this.bizResult = bizResult;
    }

    public void saveBizResult(Object bizResult) {
        setBizResult(bizResult);
        savePersistentInfo(BIZ_RESULT_KEY, bizResult);
    }

    public Object putTransientInfo(String key, Object value) {
        return transientInfo.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTransientCache(String key, Class<T> cls) {
        return (T) transientInfo.get(key);
    }

    public void savePersistentInfo(String key, Object value) {
        Repository repository = SagaApplication.getRepository();
        repository.saveContextInfo(id, key, value);
    }

    public <T> T getPersistentInfo(String key, Class<T> cls) {
        return repository.loadContextInfo(id, key, cls);
    }

    public Class<? extends SagaTx> getCurrentTx() {
        return currentTx;
    }

    public void setCurrentTx(Class<? extends SagaTx> currentTx) {
        this.currentTx = currentTx;
    }

    public void saveCurrentTx(Class<? extends SagaTx> currentTx) {
        setCurrentTx(currentTx);
        repository.saveCurrentTx(this.id, currentTx);
    }

    public Class<? extends SagaTx> getPreExecutedTx() {
        return preExecutedTx;
    }

    public void setPreExecutedTx(Class<? extends SagaTx> preExecutedTx) {
        this.preExecutedTx = preExecutedTx;
    }

    public void savePreExecutedTx(Class<? extends SagaTx> preExecutedTx) {
        setPreExecutedTx(preExecutedTx);
        repository.savePreExecutedTx(this.id, preExecutedTx);
    }

    public Class<? extends SagaTx> getPreCompensatedTx() {
        return preCompensatedTx;
    }

    public void setPreCompensatedTx(Class<? extends SagaTx> preCompensatedTx) {
        this.preCompensatedTx = preCompensatedTx;
    }

    public void savePreCompensatedTx(Class<? extends SagaTx> preCompensatedTx) {
        setPreCompensatedTx(preCompensatedTx);
        repository.savePreCompensatedTx(this.id, preCompensatedTx);
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(int triggerCount) {
        this.triggerCount = triggerCount;
    }

    public Date getNextTriggerTime() {
        return nextTriggerTime;
    }

    public void setNextTriggerTime(Date nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * 将触发次数+1，并保存到存储中。
     * 
     * @return
     */
    public int incrementTriggerCount() {
        repository.incrementTriggerCount(id);
        triggerCount++;
        return triggerCount;
    }

    /**
     * 计算下一次的触发时间，并保存到存储中。
     * 
     * @return
     */
    public Date saveNextTriggerTime() {
        Date nextTriggerTime = calculationNextTriggerTime();
        repository.saveNextTriggerTime(id, nextTriggerTime);
        this.nextTriggerTime = nextTriggerTime;
        return nextTriggerTime;
    }

    public Date calculationNextTriggerTime() {

        int interval = 0;

        int[] triggerInterval = saga.getTriggerInterval();
        int length = saga.getTriggerInterval().length;
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
        if (isLocked()) {
            return true;
        } else {
            Integer lockTimeout = saga.getLockTimeout();
            boolean lock = repository.lock(id, triggerId, lockTimeout);
            locked.set(lock);
            return lock;
        }
    }

    /**
     * 释放锁
     * 
     * @return true 为释放锁成功；false为释放失败，比如持有锁超时了
     */
    public boolean unlock() {
        if (!isLocked()) {
            return true;
        } else {
            boolean unlock = repository.unlock(id, triggerId);
            if (unlock) {
                locked.set(false);
            }
            return unlock;
        }
    }

    public boolean isLocked() {
        return locked.get();
    }
}
