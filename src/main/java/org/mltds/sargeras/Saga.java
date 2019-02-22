package org.mltds.sargeras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mltds.sargeras.utils.Pair;

/**
 * Saga 代表着一个长事务（LLT,long live transaction），由多个小事务（Tx）有序组成。<br/>
 * 利用 {@link SagaBuilder} 构建，被构建后不可更改，线程安全。
 *
 * @author sunyi 2019/2/15.
 */
public class Saga {

    private final String appName;
    private final String bizName;

    private Long timeoutMills;

    private List<SagaTx> txs = new ArrayList<>();
    private List<SagaListener> listeners = new ArrayList<>();

    Saga(String appName, String bizName) {
        this.appName = appName;
        this.bizName = bizName;
    }

    public String getAppName() {
        return appName;
    }

    public String getBizName() {
        return bizName;
    }

    public String getKeyName() {
        return this.appName + "-" + this.bizName;
    }

    void addTx(SagaTx tx) {
        txs.add(tx);
    }

    /**
     * 执行一个长事务
     */
    public Pair<SagaTxStatus, Object> run(String bizId, Object bizParam) {
        // TODO build context

        return null;
    }

    public List<SagaListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    void addListener(SagaListener listener) {
        this.listeners.add(listener);
    }

}
