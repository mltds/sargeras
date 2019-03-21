package org.mltds.sargeras.aop;

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.expectation.Failure;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author sunyi.
 */
@Aspect
public class SagaAspect {

    @Autowired
    private SagaAopHolder aopHolder;

    @Autowired
    private SagaAopComponent aopComponent;

    @Pointcut("@annotation(org.mltds.sargeras.api.annotation.Saga)")
    public void sagaAspect() {
    }

    @Before("sagaAspect()")
    public void before(JoinPoint joinPoint) {

        Saga saga = aopComponent.getSaga(joinPoint);
        String bizId = aopComponent.getBizId(joinPoint);

        SagaContext context = SagaContext.loadContext(saga.getAppName(), saga.getBizName(), bizId);

        if (context == null) {
            context = SagaContext.newContext(saga, bizId);
            context.saveAndLock();
        }
        aopHolder.setContext(context);

    }

    @Around("sagaAspect()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        SagaContext context = aopHolder.getContext();

        SagaStatus status = context.getStatus();

        if (status.equals(SagaStatus.INIT) || status.equals(SagaStatus.EXECUTING) || status.equals(SagaStatus.EXECUTE_SUCC)) {

            return proceedingJoinPoint.proceed();

        } else if (status.equals(SagaStatus.COMPENSATING)) {
            // 补偿中，进行补偿
            aopComponent.compensate();
            // 补偿没有结果，返回null
            return null;
        } else if (status.equals(SagaStatus.COMPENSATE_SUCC) || status.equals(SagaStatus.COMPENSATE_FAIL) || status.equals(SagaStatus.OVERTIME)) {
            // 处于终态的流程且已经丢失了当时的原因或异常，直接 Return；
            return null;
        } else {
            throw new SagaException("执行时发现不合理的状态，Saga Record ID：" + context.getRecordId() + ", Status: " + status);
        }

    }

    @AfterReturning("sagaAspect()")
    public void afterReturning(JoinPoint joinPoint) {
        // 记录 SagaRecord 为成功
        SagaContext context = aopHolder.getContext();
        context.saveStatus(SagaStatus.EXECUTE_SUCC);

        aopHolder.removeContext();
    }

    @AfterThrowing(pointcut = "sagaAspect()", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {

        if (throwable instanceof Failure) {
            aopComponent.compensate();
        } else {
            SagaContext context = aopHolder.getContext();
            Date nextTriggerTime = context.saveNextTriggerTime();
            if (nextTriggerTime.after(context.getExpireTime())) {
                context.saveStatus(SagaStatus.OVERTIME);
            }
        }
    }

}