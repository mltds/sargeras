//package org.mltds.sargeras.spi.service.defaultimpl;
//
//import java.util.List;
//
//import org.mltds.sargeras.api.SagaContext;
//import org.mltds.sargeras.api.SagaTx;
//import org.mltds.sargeras.api.SagaTxStatus;
//import org.mltds.sargeras.api.exception.SagaException;
//import org.mltds.sargeras.api.exception.SagaIncompatibleException;
//
//public class TxChain implements SagaTx {
//
//    private List<SagaTx> txList;
//
//    private ListenerChain listenerChain;
//
//    TxChain(List<SagaTx> txList, ListenerChain listenerChain) {
//        this.txList = txList;
//        this.listenerChain = listenerChain;
//    }
//
//    @Override
//    public SagaTxStatus execute(SagaContext context) {
//
//        Class<? extends SagaTx> currentTx = context.getCurrentTx();
//
//        SagaTxStatus txStatus = null;
//
//        boolean canExecute = false;
//        for (SagaTx tx : txList) {
//
//            Class<? extends SagaTx> txCls = tx.getClass();
//
//            if (!canExecute) {
//                if (currentTx == null) {
//                    // 首次执行，从当前第一个 TX 开始执行
//                    canExecute = true;
//                } else if (txCls.equals(currentTx)) {
//                    // 之前执行过，那么从记录中的的当前 TX 开始执行
//                    canExecute = true;
//                } else {
//                    continue;
//                }
//            }
//
//            if (!txCls.equals(currentTx)) {
//                context.saveCurrentTxAndParam(txCls);
//            }
//
//            listenerChain.beforeExecute(context, tx);
//            try {
//                txStatus = tx.execute(context);
//            } catch (Exception e) {
//                txStatus = SagaTxStatus.PROCESSING;// 执行过程中发生异常，因异常不能视为业务结果，故认为状态为处理中
//                listenerChain.onException(context, e);
//            }
//            listenerChain.afterExecute(context, tx, txStatus);
//
//            if (SagaTxStatus.SUCCESS.equals(txStatus)) {
//                // 执行成功，更新相关信息，然后继续执行
//                context.savePreExecutedTx(txCls);
//                continue;// 为了可读性留着
//            } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
//                // 处理中，结果未知，不继续执行
//                break;
//            } else if (SagaTxStatus.FAILURE.equals(txStatus)) {
//                context.savePreExecutedTx(txCls); // 执行失败，这个TX作为补偿起始点
//                break;
//            } else {
//                throw new SagaException("SagaTx： " + txCls.getSimpleName() + " execute 方法返回错误的状态：" + txStatus);
//            }
//
//        }
//
//        if (!canExecute) {
//            // 没有找到执行TX的起始点,一般是因代码变动，新老流程不兼容导致的
//            throw new SagaIncompatibleException(context.getId());
//        }
//        return txStatus;
//    }
//
//    @Override
//    public SagaTxStatus compensate(SagaContext context) {
//
//        Class<? extends SagaTx> preCompensatedTx = context.getPreCompensatedTx();
//        Class<? extends SagaTx> currentTx = context.getCurrentTx();
//
//        SagaTxStatus txStatus = null;
//
//        boolean canCompensate = false;
//        for (int i = txList.size() - 1; i >= 0; i--) {
//
//            SagaTx tx = txList.get(i);
//            Class<? extends SagaTx> txCls = tx.getClass();
//
//            if (!canCompensate) {
//                if (preCompensatedTx == null && currentTx != null && txCls.equals(currentTx)) {
//                    // 首次进入补偿，当前TX是上次执行失败的TX，则将当前TX作为补偿起始点
//                    canCompensate = true;
//                } else if (preCompensatedTx != null && txCls.equals(preCompensatedTx)) {
//                    // 以前执行过某个TX补偿，从这个TX开始执行补偿
//                    canCompensate = true;
//                } else {
//                    continue;
//                }
//            }
//
//            if (!txCls.equals(currentTx)) {
//                context.saveCurrentTxAndParam(txCls);
//            }
//
//            listenerChain.beforeCompensate(context, tx);
//            try {
//                txStatus = tx.compensate(context);
//            } catch (Exception e) {
//                txStatus = SagaTxStatus.PROCESSING; // 执行过程中发生异常，因异常不能视为业务结果，故认为状态为处理中
//                listenerChain.onException(context, e);
//            }
//            listenerChain.afterCompensate(context, tx, txStatus);
//
//            if (SagaTxStatus.SUCCESS.equals(txStatus)) {
//                // 执行成功，继续执行
//                context.savePreCompensatedTx(txCls);
//                continue; // 为了可读性留着
//            } else if (SagaTxStatus.PROCESSING.equals(txStatus)) {
//                // 处理中，结果未知，不继续执行
//                break;
//            } else if (SagaTxStatus.FAILURE.equals(txStatus)) {
//                context.savePreCompensatedTx(txCls);
//                break;// 回滚补偿失败，不继续执行，流程中止
//            } else {
//                throw new SagaException("SagaTx： " + tx.getClass().getSimpleName() + " compensate 方法返回错误的状态：" + txStatus);
//            }
//        }
//
//        if (!canCompensate) {
//            // 没有找到补偿TX的起始点,一般是因代码变动，新老流程不兼容导致的
//            throw new SagaIncompatibleException(context.getId());
//        }
//
//        return txStatus;
//    }
//}