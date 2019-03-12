package org.mltds.sargeras.manager;

import java.util.List;

import org.mltds.sargeras.api.*;
import org.mltds.sargeras.exception.LockFailException;
import org.mltds.sargeras.exception.SagaException;
import org.mltds.sargeras.repository.Repository;
import org.mltds.sargeras.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这个服务类是给Saga内部使用的，不建议外部系统直接使用。 //TODO 改为接口
 * 
 * @author sunyi
 */
public class Manager implements SagaBean {

    private static final Logger logger = LoggerFactory.getLogger(Manager.class);

    private Repository repository = SagaApplication.getRepository();

    /**
     * 执行一个 Saga
     * 
     * @param saga Saga 对象
     * @param bizId 业务ID
     * @param bizParam 业务参数
     * @return Saga的状态和执行结果
     */
    public Pair<SagaStatus, Object> start(Saga saga, String bizId, Object bizParam) {
        // Build Saga Context
        SagaContext context = new SagaContext(saga);
        context.setBizId(bizId);
        context.setStatus(SagaStatus.INIT);
        Long id = repository.saveContext(context);

        context.setBizParam(bizParam);// 需要现有ContextId

        // Run
        Pair<SagaStatus, Object> result = run(context);

        return result;

    }

    public Pair<SagaStatus, Object> restart(Saga saga, String bizId) {

        // TODO load context
        SagaContext context = null;

        // Run
        Pair<SagaStatus, Object> result = run(context);

        return result;
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
    private Pair<SagaStatus, Object> run(SagaContext context) {
        List<SagaTx> txList = context.getSaga().getTxList();
        List<SagaListener> listenerList = context.getSaga().getListenerList();

        ListenerChain listenerChain = new ListenerChain(listenerList);
        TxChain txChain = new TxChain(txList, listenerChain);

        SagaStatus status = context.getStatus();

        boolean lock = context.lock();
        if (!lock) {
            throw new LockFailException(context.getId(), context.getOnceId());
        }

        try {
            boolean isNewStart = SagaStatus.INIT.equals(status);
            if (isNewStart) {
                status = SagaStatus.EXECUTING;
                context.saveStatus(status);
                listenerChain.onStart(context);
            } else {
                listenerChain.onRestart(context);
            }

            if (SagaStatus.EXECUTING.equals(status)) {

                SagaTxStatus txStatus = txChain.execute(context);

                if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                    status = SagaStatus.EXECUTE_SUCC;
                    context.saveStatus(status);
                } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                    // do nothing
                } else if (SagaTxStatus.EXE_FAIL_TO_COMP.equals(txStatus)) {
                    status = SagaStatus.COMPENSATING;
                    context.saveStatus(status);
                    listenerChain.onToComp(context);
                }
            }

            if (SagaStatus.COMPENSATING.equals(status)) {

                SagaTxStatus txStatus = txChain.compensate(context);

                if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                    status = SagaStatus.COMPENSATE_SUCC;
                    context.saveStatus(status);
                } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                    // do nothing
                } else if (SagaTxStatus.COMP_FAIL_TO_FINAL.equals(txStatus)) {
                    status = SagaStatus.COMPENSATE_FAIL;
                    context.saveStatus(status);
                    listenerChain.onToFinal(context);
                }
            }
        } finally {
            context.unlock();
        }

        // Return Result
        Object bizResult = context.getBizResult(Object.class);
        Pair<SagaStatus, Object> result = new Pair<>(status, bizResult);
        return result;
    }

    private class TxChain implements SagaTx {

        private List<SagaTx> txList;

        private ListenerChain listenerChain;

        TxChain(List<SagaTx> txList, ListenerChain listenerChain) {
            this.txList = txList;
            this.listenerChain = listenerChain;
        }

        @Override
        public SagaTxStatus execute(SagaContext context) {

            Long id = context.getId();
            Class<? extends SagaTx> preExecutedTx = context.getPreExecutedTx();

            SagaTxStatus txStatus = null;

            boolean canExecute = false;
            for (SagaTx tx : txList) {

                Class<? extends SagaTx> txCls = tx.getClass();

                if (!canExecute) {
                    if (preExecutedTx == null) {
                        // 首次执行，从当前 TX 开始执行
                        canExecute = true;
                    } else if (tx.getClass().equals(preExecutedTx)) {
                        // 之前执行过，那么从下一个 TX 开始执行
                        canExecute = true;
                        continue;
                    } else {
                        continue;
                    }
                }

                context.saveCurrentTx(txCls);

                listenerChain.beforeExecute(context, tx);
                try {
                    txStatus = tx.execute(context);
                } catch (Exception e) {
                    txStatus = SagaTxStatus.PROCESSING;// 执行过程中发生异常，因异常不能视为业务结果，故认为状态为处理中
                    listenerChain.onException(context, e);
                }
                listenerChain.afterExecute(context, tx, txStatus);

                if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                    // 执行成功，更新相关信息，然后继续执行
                    context.savePreExecutedTx(txCls);
                    continue;
                } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                    // 处理中，结果未知，不继续执行
                    break;
                } else if (SagaTxStatus.EXE_FAIL_TO_COMP.equals(txStatus)) {
                    context.savePreExecutedTx(txCls); // 执行失败，这个TX作为补偿起始点
                    break;
                } else {
                    throw new SagaException("SagaTx： " + txCls.getSimpleName() + " execute 方法返回错误的状态：" + txStatus);
                }

            }
            return txStatus;
        }

        @Override
        public SagaTxStatus compensate(SagaContext context) {
            Long id = context.getId();
            Class<? extends SagaTx> preExecutedTx = context.getPreExecutedTx();
            Class<? extends SagaTx> preCompensatedTx = context.getPreCompensatedTx();

            SagaTxStatus txStatus = null;

            boolean canCompensate = false;
            for (int i = txList.size() - 1; i >= 0; i--) {

                SagaTx tx = txList.get(i);
                Class<? extends SagaTx> txCls = tx.getClass();

                if (!canCompensate) {
                    if (preCompensatedTx == null && preExecutedTx != null && tx.getClass().equals(preExecutedTx)) {
                        // 首次进入补偿，当前TX是上次执行失败的TX，则将当前TX作为补偿起始点
                        canCompensate = true;
                    } else if (preCompensatedTx != null && tx.getClass().equals(preCompensatedTx)) {
                        // 以前执行过某个TX补偿，从这个TX的下一个开始执行补偿
                        canCompensate = true;
                        continue;
                    } else {
                        continue;
                    }
                }

                context.saveCurrentTx(txCls);

                try {
                    txStatus = tx.execute(context);
                } catch (Exception e) {
                    txStatus = SagaTxStatus.PROCESSING; // 执行过程中发生异常，因异常不能视为业务结果，故认为状态为处理中
                    listenerChain.onException(context, e);
                }

                if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                    context.savePreCompensatedTx(txCls);
                    continue; // 执行成功，继续执行
                } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                    // 处理中，结果未知，不继续执行
                    break;
                } else if (SagaTxStatus.COMP_FAIL_TO_FINAL.equals(txStatus)) {
                    context.savePreCompensatedTx(txCls);
                    break;// 回滚补偿失败，不继续执行，流程中止
                } else {
                    throw new SagaException("SagaTx： " + tx.getClass().getSimpleName() + " compensate 方法返回错误的状态：" + txStatus);
                }
            }

            return txStatus;
        }
    }

    private class ListenerChain implements SagaListener {

        private List<SagaListener> listenerList;

        ListenerChain(List<SagaListener> listenerList) {
            this.listenerList = listenerList;
        }

        @Override
        public void onStart(SagaContext context) {
            for (SagaListener l : listenerList) {
                try {
                    l.onStart(context);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void onRestart(SagaContext context) {
            for (SagaListener l : listenerList) {
                try {
                    l.onRestart(context);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void onToComp(SagaContext context) {
            for (SagaListener l : listenerList) {
                try {
                    l.onToComp(context);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void onToFinal(SagaContext context) {
            for (SagaListener l : listenerList) {
                try {
                    l.onToFinal(context);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void beforeExecute(SagaContext context, SagaTx tx) {
            for (SagaListener l : listenerList) {
                try {
                    l.beforeExecute(context, tx);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status) {
            for (SagaListener l : listenerList) {
                try {
                    l.afterExecute(context, tx, status);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void beforeCompensate(SagaContext context, SagaTx tx) {
            for (SagaListener l : listenerList) {
                try {
                    l.beforeCompensate(context, tx);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status) {
            for (SagaListener l : listenerList) {
                try {
                    l.afterCompensate(context, tx, status);
                } catch (Exception e) {
                    this.onException(context, e);
                }
            }
        }

        @Override
        public void onException(SagaContext context, Throwable t) {
            for (SagaListener l : listenerList) {
                try {
                    l.onException(context, t);
                } catch (Exception e) {
                    throw new SagaException("SagaListener 处理异常事件时发生异常", e);
                }
            }
        }
    }

}
