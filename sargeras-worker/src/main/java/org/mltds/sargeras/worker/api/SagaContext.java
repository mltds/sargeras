package org.mltds.sargeras.worker.api;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mltds.sargeras.common.core.SagaContextBase;
import org.mltds.sargeras.common.core.SagaStatus;
import org.mltds.sargeras.common.utils.Utils;
import org.mltds.sargeras.server.facade.ServerFacade;

/**
 * {@link SagaContext} 在JVM中的生命周期是线程级别的。<br/>
 * 即当执行一个 {@link Saga} 时，会新建一个 {@link SagaContext}，当线程结束时，这个 {@link SagaContext} 对象会被失效。<br/>
 * 如果这个 {@link Saga} 并没有执行完，当 reload 再次重试时，会根据 中的信息重新构建出这个 {@link SagaContext}，但是一个全新的JVM对象。<br/>
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

    private Map<String, Object> persistentInfoCache = new HashMap<>();
    private AtomicBoolean locked = new AtomicBoolean(false);

    private ServerFacade serverFacade = SagaApplication.getServerFacade();

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
        base.setTriggerCount(0);

        Calendar now = Calendar.getInstance();
        base.setNextTriggerTime(now.getTime());

        now.add(Calendar.SECOND, saga.getBizTimeout());
        Date expireTime = now.getTime();
        base.setExpireTime(expireTime);

        context.base = base;

        return context;
    }

    public static SagaContext loadContext(String appName, String bizName, String bizId) {
        ServerFacade serverFacade = null;
        // return serverFacade.loadContext(appName, bizName, bizId);

        return null;
    }

    public static SagaContext loadContext(long contextId) {
        ServerFacade serverFacade = null;
        // return serverFacade.loadContext(contextId);
        return null;
    }

    public void saveAndLock() {
        serverFacade.saveContextAndLock(base,saga.getLockTimeout());
        locked.set(true);
    }

    public Saga getSaga() {
        return saga;
    }

    public void saveStatus(SagaStatus status) {
        serverFacade.saveContextStatus(base.getId(), status);
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
        saveBizResult(bizResult);
        saveInfo(BIZ_RESULT_KEY, bizResult);
    }

    public void saveInfo(String key, Object value) {
        serverFacade.saveContextInfo(base.getId(), key, value);
        persistentInfoCache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T loadInfo(String key, Class<T> cls) {
        if (persistentInfoCache.containsKey(key)) {
            return (T) persistentInfoCache.get(key);
        } else {
            T v = serverFacade.loadContextInfo(base.getId(), key, cls);
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
        serverFacade.saveCurrentTx(base.getId(), name);
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
        serverFacade.savePreExecutedTx(base.getId(), name);
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
        serverFacade.savePreCompensatedTx(base.getId(), name);
        base.setPreCompensatedTxName(name);
        this.preCompensatedTx = preCompensatedTx;
    }

    /**
     * 将触发次数+1，并保存到存储中。
     *
     * @return
     */
    public void incrementTriggerCount() {
        serverFacade.incrementTriggerCount(base.getId());
        base.setTriggerCount(base.getTriggerCount() + 1);
    }

    /**
     * 计算下一次的触发时间，并保存到存储中。
     *
     * @return
     */
    public Date saveNextTriggerTime() {
        Date nextTriggerTime = calculationNextTriggerTime();
        serverFacade.saveNextTriggerTime(base.getId(), nextTriggerTime);
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
        if (locked.get()) {
            return true;
        } else {
            Integer lockTimeout = saga.getLockTimeout();
            boolean lock = serverFacade.lock(base.getId(), base.getTriggerId(), lockTimeout);
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
        if (!locked.get()) {
            return true;
        } else {
            boolean unlock = serverFacade.unlock(base.getId(), base.getTriggerId());
            if (unlock) {
                locked.set(false);
            }
            return unlock;
        }
    }

    /* base getter start */
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
