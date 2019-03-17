package org.mltds.sargeras.api;

import java.util.*;

import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.utils.Utils;

/**
 * {@link SagaContext} 在JVM中的生命周期是线程级别的。<br/>
 * 即当执行一个 {@link Saga} 时，会新建一个 {@link SagaContext}，当线程结束时，这个 {@link SagaContext} 对象会被失效。<br/>
 * 如果这个 {@link Saga} 并没有执行完，当 reload 再次重试时，会根据 {@link Manager} 中的信息重新构建出这个 {@link SagaContext}，但是一个全新的JVM对象。<br/>
 * 
 * @author sunyi
 */
public class SagaContext {

    private static final String BIZ_PARAM_KEY = "BIZ_PARAM";
    private static final String BIZ_RESULT_KEY = "BIZ_RESULT";

    private Saga saga;
    private SagaContextBase base;

    private Class<? extends SagaTx> currentTx;
    private Class<? extends SagaTx> preExecutedTx;
    private Class<? extends SagaTx> preCompensatedTx;

    private boolean lock = false;
    private Map<String, Object> persistentInfoCache = new HashMap<>();

    private Manager manager = SagaApplication.getRepository();

    private SagaContext() {

    }

    public static SagaContext newContext(Saga saga, String bizId) {

        SagaContext context = new SagaContext();
        context.saga = saga;

        SagaContextBase base = new SagaContextBase();
        base.setAppName(saga.getAppName());
        base.setBizName(saga.getBizName());
        base.setBizId(bizId);
        base.setStatus(SagaStatus.INIT);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        base.setTriggerId(triggerId);
        base.setTriggerCount(0);

        Calendar now = Calendar.getInstance();
        base.setNextTriggerTime(now.getTime());
        now.add(Calendar.SECOND, saga.getBizTimeout());

        now = Calendar.getInstance();
        int bizTimeout = saga.getBizTimeout();
        now.add(Calendar.SECOND, bizTimeout);
        base.setExpireTime(now.getTime());

        context.base = base;

        return context;
    }

    public static SagaContext loadContext(String appName, String bizName, String bizId) {
        SagaContext context = new SagaContext();

        Manager manager = SagaApplication.getRepository();
        SagaContextBase sagaContextBase = manager.loadContext(appName, bizName, bizId);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        sagaContextBase.setTriggerId(triggerId);
        context.base = sagaContextBase;

        Saga saga = SagaApplication.getSaga(appName, bizName);
        context.saga = saga;

        return context;
    }

    public static SagaContext loadContext(long contextId) {
        SagaContext context = new SagaContext();

        Manager manager = SagaApplication.getRepository();
        SagaContextBase sagaContextBase = manager.loadContext(contextId);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        sagaContextBase.setTriggerId(triggerId);

        context.base = sagaContextBase;

        Saga saga = SagaApplication.getSaga(context.getAppName(), context.getBizName());
        context.saga = saga;

        return context;
    }

    public void saveAndLock() {
        long id = manager.saveContextAndLock(base, saga.getLockTimeout());
        lock = true;
        base.setId(id);
    }

    public Saga getSaga() {
        return saga;
    }

    public void saveStatus(SagaStatus status) {
        manager.saveContextStatus(base.getId(), status);
        base.setStatus(status);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBizParam(Class<T> t) {
        return loadInfo(BIZ_PARAM_KEY, t);
    }

    public void saveBizParam(Object bizParam) {
        saveInfo(BIZ_PARAM_KEY, bizParam);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBizResult(Class<T> t) {
        return loadInfo(BIZ_RESULT_KEY, t);
    }

    public void saveBizResult(Object bizResult) {
        saveInfo(BIZ_RESULT_KEY, bizResult);
    }

    public void saveInfo(String key, Object value) {
        manager.saveContextInfo(base.getId(), key, value);
        persistentInfoCache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T loadInfo(String key, Class<T> cls) {
        if (persistentInfoCache.containsKey(key)) {
            return (T) persistentInfoCache.get(key);
        } else {
            T v = manager.loadContextInfo(base.getId(), key, cls);
            persistentInfoCache.put(key, v);
            return v;
        }
    }

    public Class<? extends SagaTx> getCurrentTx() {
        if (currentTx == null && base.getCurrentTxName() != null) {
            Class cls = Utils.loadClass(base.getCurrentTxName());
            this.currentTx = cls;
        }
        return currentTx;
    }

    public void saveCurrentTx(Class<? extends SagaTx> currentTx) {
        String name = currentTx.getName();
        manager.saveCurrentTx(base.getId(), name);
        base.setCurrentTxName(name);
        this.currentTx = currentTx;
    }

    public Class<? extends SagaTx> getPreExecutedTx() {
        if (preExecutedTx == null && base.getPreExecutedTxName() != null) {
            Class cls = Utils.loadClass(base.getPreExecutedTxName());
            this.preExecutedTx = cls;
        }
        return preExecutedTx;
    }

    public void savePreExecutedTx(Class<? extends SagaTx> preExecutedTx) {
        String name = preExecutedTx.getName();
        manager.savePreExecutedTx(base.getId(), name);
        base.setPreExecutedTxName(name);
        this.preExecutedTx = preExecutedTx;

    }

    public Class<? extends SagaTx> getPreCompensatedTx() {
        if (preCompensatedTx == null && base.getPreCompensatedTxName() != null) {
            Class cls = Utils.loadClass(base.getPreCompensatedTxName());
            this.preCompensatedTx = cls;
        }
        return preCompensatedTx;
    }

    public void savePreCompensatedTx(Class<? extends SagaTx> preCompensatedTx) {
        String name = preCompensatedTx.getName();
        manager.savePreCompensatedTx(base.getId(), name);
        base.setPreCompensatedTxName(name);
        this.preCompensatedTx = preCompensatedTx;
    }

    /**
     * 将触发次数+1，并保存到存储中。
     *
     * @return
     */
    public void incrementTriggerCount() {
        manager.incrementTriggerCount(base.getId());
        base.setTriggerCount(base.getTriggerCount() + 1);
    }

    /**
     * 计算下一次的触发时间，并保存到存储中。
     *
     * @return
     */
    public Date saveNextTriggerTime() {
        Date nextTriggerTime = calculationNextTriggerTime();
        manager.saveNextTriggerTime(base.getId(), nextTriggerTime);
        base.setNextTriggerTime(nextTriggerTime);
        return nextTriggerTime;
    }

    public Date calculationNextTriggerTime() {

        int interval = 0;

        int[] triggerInterval = saga.getTriggerInterval();
        int length = saga.getTriggerInterval().length;
        int triggerCount = base.getTriggerCount();

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
            boolean lock = manager.lock(base.getId(), base.getTriggerId(), lockTimeout);
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
            boolean unlock = manager.unlock(base.getId(), base.getTriggerId());
            if (unlock) {
                this.lock = false;
            }
            return unlock;
        }
    }

    /* base getter start */

    /**
     * 业务系统请勿直接使用
     */
    public SagaContextBase getBase() {
        return base;
    }

    public Long getId() {
        return base.getId();
    }

    public String getAppName() {
        return base.getAppName();
    }

    public String getBizName() {
        return base.getBizName();
    }

    public String getBizId() {
        return base.getBizId();
    }

    public SagaStatus getStatus() {
        return base.getStatus();
    }

    public String getTriggerId() {
        return base.getTriggerId();
    }

    public int getTriggerCount() {
        return base.getTriggerCount();
    }

    public Date getNextTriggerTime() {
        return base.getNextTriggerTime();
    }

    public Date getCreateTime() {
        return base.getCreateTime();
    }

    public Date getExpireTime() {
        return base.getExpireTime();
    }
    /* base getter end */

}
