package org.mltds.sargeras.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.model.SagaTxRecord;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author sunyi.
 */
@Aspect
@Component
public class SagaTxAspect {

    @Autowired
    private SagaAopHolder aopHolder;

    @Autowired
    private Serializer serializer;

    @Autowired
    private SagaAopComponent aopComponent;

    @Pointcut("@annotation(org.mltds.sargeras.api.annotation.SagaTx)")
    public void sagaTxAspect() {
    }

    @Around("sagaTxAspect()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        SagaContext context = aopHolder.getContext();
        if (context.getStatus().equals(SagaStatus.INIT)) {
            context.saveStatus(SagaStatus.EXECUTING);
        }

        if (context.isFirstExecute()) {
            // Saga 首次执行，保存执行记录和参数
            SagaTxRecord txRecord = aopComponent.saveTxRecordAndParam(proceedingJoinPoint);
            return aopComponent.doExecute(proceedingJoinPoint, txRecord);
        } else {
            SagaTxRecord previousTxRecord = aopComponent.getPreviousTxRecord(proceedingJoinPoint);
            SagaTxRecord txRecord = aopComponent.saveTxRecordAndParam(proceedingJoinPoint);
            if (previousTxRecord == null) {
                // SagaTx 首次执行，保存执行记录和参数
                return aopComponent.doExecute(proceedingJoinPoint, txRecord);
            } else {
                SagaTxStatus status = previousTxRecord.getStatus();

                if (status.equals(SagaTxStatus.SUCCESS)) {
                    // 之前执行成功过，则获取结果
                    return aopComponent.getTxRecordResult(previousTxRecord.getId());
                } else {
                    // 如果之前执行没有成功，需要再次执行
                    return aopComponent.doExecute(proceedingJoinPoint, txRecord);
                }
            }
        }
    }

}
