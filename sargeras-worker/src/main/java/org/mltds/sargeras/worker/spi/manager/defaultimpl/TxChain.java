package org.mltds.sargeras.worker.spi.manager.defaultimpl;

import java.util.List;

import org.mltds.sargeras.worker.api.SagaContext;
import org.mltds.sargeras.worker.api.SagaTx;
import org.mltds.sargeras.worker.api.SagaTxStatus;
import org.mltds.sargeras.common.exception.SagaException;

public class TxChain implements SagaTx {

    private List<SagaTx> txList;

    private ListenerChain listenerChain;

    TxChain(List<SagaTx> txList, ListenerChain listenerChain) {
        this.txList = txList;
        this.listenerChain = listenerChain;
    }

    @Override
    public SagaTxStatus execute(SagaContext context) {

        Class<? extends SagaTx> currentTx = context.getCurrentTx();

        SagaTxStatus txStatus = null;

        boolean canExecute = false;
        for (SagaTx tx : txList) {

            Class<? extends SagaTx> txCls = tx.getClass();

            if (!canExecute) {
                if (currentTx == null) {
                    // 首次执行，从当前第一个 TX 开始执行
                    canExecute = true;
                } else if (txCls.equals(currentTx)) {
                    // 之前执行过，那么从记录中的的当前 TX 开始执行
                    canExecute = true;
                } else {
                    continue;
                }
            }

            if (!txCls.equals(currentTx)) {
                context.saveCurrentTx(txCls);
            }

            listenerChain.beforeExecute(context, tx);
            try {
                txStatus = tx.execute(context);
            } catch (Exception e) {
                txStatus = SagaTxStatus.PROCESSING;// 执行过程中发生异常，因异常不能视为业务结果，故认为状态为处理中
                listenerChain.onException(context, e);
            }

            if (txStatus == null) {
                throw new SagaException(txCls.getSimpleName() + "执行没有返回状态结果");
            }

            listenerChain.afterExecute(context, tx, txStatus);

            if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                // 执行成功，更新相关信息，然后继续执行
                context.savePreExecutedTx(txCls);
            } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                // 处理中，结果未知，不继续执行
                return txStatus;
            } else if (SagaTxStatus.FAILURE.equals(txStatus)) {
                context.savePreExecutedTx(txCls); // 执行失败，这个TX作为补偿起始点
                return txStatus;
            } else {
                throw new SagaException("Tx： " + txCls.getSimpleName() + " execute 方法返回错误的状态：" + txStatus);
            }

        }
        return txStatus;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {

        Class<? extends SagaTx> preCompensatedTx = context.getPreCompensatedTx();
        Class<? extends SagaTx> currentTx = context.getCurrentTx();

        SagaTxStatus txStatus = null;

        boolean canCompensate = false;
        for (int i = txList.size() - 1; i >= 0; i--) {

            SagaTx tx = txList.get(i);
            Class<? extends SagaTx> txCls = tx.getClass();

            if (!canCompensate) {
                if (preCompensatedTx == null && currentTx != null && txCls.equals(currentTx)) {
                    // 首次进入补偿，当前TX是上次执行失败的TX，则将当前TX作为补偿起始点
                    canCompensate = true;
                } else if (preCompensatedTx != null && txCls.equals(preCompensatedTx)) {
                    // 以前执行过某个TX补偿，从这个TX开始执行补偿
                    canCompensate = true;
                } else {
                    continue;
                }
            }

            if (!txCls.equals(currentTx)) {
                context.saveCurrentTx(txCls);
            }

            listenerChain.beforeCompensate(context, tx);
            try {
                txStatus = tx.compensate(context);
            } catch (Exception e) {
                txStatus = SagaTxStatus.PROCESSING; // 执行过程中发生异常，因异常不能视为业务结果，故认为状态为处理中
                listenerChain.onException(context, e);
            }

            if (txStatus == null) {
                throw new SagaException(txCls.getSimpleName() + "执行没有返回状态结果");
            }

            listenerChain.afterCompensate(context, tx, txStatus);

            if (SagaTxStatus.SUCCESS.equals(txStatus)) {
                // 执行成功，继续执行
                context.savePreCompensatedTx(txCls);
            } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
                // 处理中，结果未知，不继续执行
                return txStatus;
            } else if (SagaTxStatus.FAILURE.equals(txStatus)) {
                context.savePreCompensatedTx(txCls);
                return txStatus;// 回滚补偿失败，不继续执行，流程中止
            } else {
                throw new SagaException("Tx： " + tx.getClass().getSimpleName() + " compensate 方法返回错误的状态：" + txStatus);
            }
        }

        return txStatus;
    }
}