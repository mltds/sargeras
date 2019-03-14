package org.mltds.sargeras.manager.defaultimpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mltds.sargeras.api.*;
import org.mltds.sargeras.exception.SagaContextLockFailException;
import org.mltds.sargeras.listener.SagaListener;
import org.mltds.sargeras.manager.Manager;
import org.mltds.sargeras.repository.Repository;

/**
 * @author sunyi.
 */
public class DefaultManager implements Manager {

    private Repository repository = SagaApplication.getRepository();

    @Override
    public SagaResult start(Saga saga, String bizId, Object bizParam) throws SagaContextLockFailException {
        // Build Saga Context
        SagaContext context = new SagaContext(saga);
        context.setBizId(bizId);
        context.setStatus(SagaStatus.INIT);
        context.setTriggerCount(0);
        Calendar now = Calendar.getInstance();

        context.setNextTriggerTime(now.getTime());

        now.add(Calendar.SECOND, saga.getBizTimeout());
        Date expireTime = now.getTime();
        context.setExpireTime(expireTime);

        repository.saveContextAndLock(context);// 保存并生成ID

        context.saveBizParam(bizParam);// 需要先有 ContextId 才能 Save

        // Run
        return run(context);
    }

    @Override
    public SagaResult restart(Saga saga, String bizId) throws SagaContextLockFailException {
        SagaContext context = repository.loadContext(saga.getAppName(), saga.getBizName(), bizId);
        // Run
        return run(context);
    }

    @Override
    public void pollRetry(long contextId) {

        SagaContext context = repository.loadContext(contextId);
        boolean lock = context.lock();
        if (!lock) {
            return; // 获取锁失败，放弃执行
        }

        try {
            if ((new Date()).before(context.getNextTriggerTime())) {
                return; // 当前时间早于下一次触发时间，放弃执行
            }

            run(context);// 执行

        } finally {
            context.unlock();
        }
    }

    /**
     * 运行一个 Saga<br/>
     * ---------------------------------例如--------------------------------- <br/>
     * <i>TX1.execute --SUCCESS--> TX2.execute --SUCCESS--> TX3.execute </i><br/>
     * 假设 TX3.execute 返回 EXE_FAIL_TO_COMP，那么会进行逆向的补偿回滚。<br/>
     * <i>TX3.compensate --SUCCESS--> TX2.compensate --COMP_FAIL_TO_FINAL--></i> <br/>
     * 假如 TX2.compensate 返回 COMP_FAIL_TO_FINAL，那么整个流程中止，需要人工介入。
     *
     */
    protected SagaResult run(SagaContext context) throws SagaContextLockFailException {
        List<SagaTx> txList = context.getSaga().getTxList();
        List<SagaListener> listenerList = context.getSaga().getListenerList();

        ListenerChain listenerChain = new ListenerChain(listenerList);
        TxChain txChain = new TxChain(txList, listenerChain);

        SagaStatus status = context.getStatus();

        boolean lock = context.lock();
        if (!lock) {
            throw new SagaContextLockFailException(context.getId(), context.getTriggerId());
        }

        try {

            context.incrementTriggerCount();

            boolean isFirstStart = SagaStatus.INIT.equals(status);
            if (isFirstStart) {
                status = SagaStatus.EXECUTING;
                context.saveStatus(status);
                listenerChain.onStart(context);
            } else {
                listenerChain.onRestart(context);
            }

            SagaTxStatus txStatus = null;

            if (SagaStatus.EXECUTING.equals(status)) {

                txStatus = txChain.execute(context);

                if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                    status = SagaStatus.EXECUTE_SUCC;
                    context.saveStatus(status);
                    listenerChain.onExecuteSucc(context);
                } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                    if ((new Date()).after(context.getExpireTime())) {
                        status = SagaStatus.OVERTIME;
                        context.saveStatus(status);
                        listenerChain.onOvertime(context);
                    } else {
                        context.saveNextTriggerTime();
                    }
                } else if (SagaTxStatus.EXE_FAIL_TO_COMP.equals(txStatus)) {
                    status = SagaStatus.COMPENSATING;
                    context.saveStatus(status);
                    listenerChain.onExeFailToComp(context);
                }
            }

            if (SagaStatus.COMPENSATING.equals(status)) {

                txStatus = txChain.compensate(context);

                if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                    status = SagaStatus.COMPENSATE_SUCC;
                    context.saveStatus(status);
                    listenerChain.onCompensateSucc(context);
                } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                    if ((new Date()).after(context.getExpireTime())) {
                        status = SagaStatus.OVERTIME;
                        context.saveStatus(status);
                        listenerChain.onOvertime(context);
                    } else {
                        context.saveNextTriggerTime();
                    }
                } else if (SagaTxStatus.COMP_FAIL_TO_FINAL.equals(txStatus)) {
                    status = SagaStatus.COMPENSATE_FAIL;
                    context.saveStatus(status);
                    listenerChain.onComFailToFinal(context);
                }
            }

        } finally {
            context.unlock();
        }

        // Return Result

        SagaResult result = new SagaResult();
        result.setSaga(context.getSaga());
        result.setContext(context);
        result.setStatus(status);

        return result;
    }

}
