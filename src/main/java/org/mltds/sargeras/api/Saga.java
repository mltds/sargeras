package org.mltds.sargeras.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mltds.sargeras.manager.Manager;
import org.mltds.sargeras.utils.Pair;

/**
 * Saga 代表着一个长事务（LLT,long live transaction），由多个小事务（Tx）有序组成。<br/>
 * 利用 {@link SagaBuilder} 构建，被构建后不可更改，线程安全。
 *
 * @author sunyi
 */
public class Saga {

    /**
     * 默认每次执行时占有锁的最长时间，100秒。
     */
    private static final Integer DEFAULT_LOCK_TIMEOUT = 100;
    /**
     * 默认每笔业务的超时时间，1天。
     */
    private static final Integer DEFAULT_BIZ_TIMEOUT = 60 * 60 * 24;

    private final String appName;
    private final String bizName;
    private Integer lockTimeout = DEFAULT_LOCK_TIMEOUT;
    private Integer bizTimeout = DEFAULT_BIZ_TIMEOUT;

    private List<SagaTx> txList = new ArrayList<>();
    private List<SagaListener> listenerList = new ArrayList<>();

    Saga(String appName, String bizName) {
        this.appName = appName;
        this.bizName = bizName;
    }

    public static String getKeyName(String appName, String bizName) {
        return appName + "-" + bizName;
    }

    public String getAppName() {
        return appName;
    }

    public String getBizName() {
        return bizName;
    }

    public String getKeyName() {
        return getKeyName(this.appName, this.bizName);
    }

    void addTx(SagaTx tx) {
        txList.add(tx);
    }

    public List<SagaTx> getTxList() {
        return Collections.unmodifiableList(txList);
    }

    public List<SagaListener> getListenerList() {
        return Collections.unmodifiableList(listenerList);
    }

    void addListener(SagaListener listener) {
        this.listenerList.add(listener);
    }

    public Integer getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(Integer lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public Integer getBizTimeout() {
        return bizTimeout;
    }

    public void setBizTimeout(Integer bizTimeout) {
        this.bizTimeout = bizTimeout;
    }

    /**
     * 执行一个长事务
     */
    public Pair<SagaStatus, Object> start(String bizId, Object bizParam) {
        Manager manager = SagaApplication.getManager();
        Pair<SagaStatus, Object> result = manager.start(this, bizId, bizParam);
        return result;

    }

}