package org.mltds.sargeras.aop;

import java.util.Date;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.mltds.sargeras.api.*;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.expectation.Failure;
import org.mltds.sargeras.api.listener.SagaListener;
import org.mltds.sargeras.api.model.MethodInfo;
import org.mltds.sargeras.api.model.ParamInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author sunyi.
 */
@Aspect
public class SagaAspect implements ApplicationContextAware {

    @Autowired
    private SagaAopHolder aopHolder;

    @Autowired
    private SagaAopComponent aopComponent;

    private ApplicationContext applicationContext;

    @Pointcut("@annotation(org.mltds.sargeras.api.annotation.Saga)")
    public void sagaAspect() {
    }

    @Before("sagaAspect()")
    public void before(JoinPoint joinPoint) {

        org.mltds.sargeras.api.annotation.Saga sagaAnnotation = aopComponent.getSaga(joinPoint);

        String appName = sagaAnnotation.appName();
        String bizName = sagaAnnotation.bizName();
        String bizId = aopComponent.getBizId(joinPoint);

        SagaContext context = SagaContext.loadContext(appName, bizName, bizId);

        if (context == null) {// 首次执行
            MethodInfo methodInfo = aopComponent.getMethodInfo(joinPoint);
            List<ParamInfo> paramInfoList = aopComponent.getParamInfoList(methodInfo);
            Saga saga = getSaga(joinPoint);
            context = SagaContext.newContext(saga);
            context.firstTrigger(bizId, methodInfo, paramInfoList);
        } else {
            context.trigger();

        }

        aopHolder.setContext(context);

    }

    @Around("sagaAspect()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        SagaContext context = aopHolder.getContext();

        SagaStatus status = context.getStatus();

        if (status.equals(SagaStatus.EXECUTING) || status.equals(SagaStatus.EXECUTE_SUCC)) {

            return proceedingJoinPoint.proceed();

        } else if (status.equals(SagaStatus.COMPENSATING)) {
            // 补偿中，进行补偿
            aopComponent.compensate();
            // 因为补偿是分别调用的，并没有结果，所以返回null
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
        SagaStatus status = context.getStatus();
        if (status.equals(SagaStatus.EXECUTING)) {
            context.saveStatus(SagaStatus.EXECUTE_SUCC);
        } else if (status.equals(SagaStatus.COMPENSATING)) {
            context.saveStatus(SagaStatus.COMPENSATE_SUCC);
        }
        context.triggerOver();
        aopHolder.removeContext();
    }

    @AfterThrowing(pointcut = "sagaAspect()", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {

        SagaContext context = aopHolder.getContext();
        SagaStatus status = context.getStatus();

        if (throwable instanceof Failure) {
            if (status.equals(SagaStatus.EXECUTING)) {
                aopComponent.compensate();
            } else if (status.equals(SagaStatus.COMPENSATING)) {
                context.saveStatus(SagaStatus.COMPENSATE_FAIL);
            }

        } else {
            Date nextTriggerTime = context.getNextTriggerTime();
            if (nextTriggerTime.after(context.getExpireTime())) {
                context.saveStatus(SagaStatus.OVERTIME);
            }
        }
        context.triggerOver();
        aopHolder.removeContext();
    }

    private Saga getSaga(JoinPoint joinPoint) {

        org.mltds.sargeras.api.annotation.Saga anno = aopComponent.getSaga(joinPoint);

        String appName = anno.appName();
        String bizName = anno.bizName();

        Saga saga = SagaApplication.getSaga(appName, bizName);
        if (saga != null) {
            return saga;
        }

        MethodInfo methodInfo = aopComponent.getMethodInfo(joinPoint);
        saga = buildSaga(joinPoint.getThis(), anno, methodInfo);

        return saga;

    }

    private Saga buildSaga(Object bean, org.mltds.sargeras.api.annotation.Saga anno, MethodInfo methodInfo) {

        String appName = anno.appName();
        String bizName = anno.bizName();
        int bizTimeout = anno.bizTimeout();
        int lockTimeout = anno.lockTimeout();
        String triggerInterval = anno.triggerInterval();
        Class<? extends SagaListener>[] listeners = anno.listeners();

        SagaBuilder builder = SagaBuilder.newBuilder();
        builder.setAppName(appName)//
                .setBizName(bizName)//
                .setCls(methodInfo.getCls())//
                .setBean(bean)//
                .setMethod(methodInfo.getMethod())//
                .setParamTypes(methodInfo.getParameterTypes())//
                .setBizTimeout(bizTimeout)//
                .setLockTimeout(lockTimeout)//
                .setTriggerInterval(triggerInterval)//
        ;

        for (Class<? extends SagaListener> listener : listeners) {
            SagaListener l = applicationContext.getBean(listener);
            if (l == null) {
                throw new SagaException(methodInfo.getCls().getSimpleName() + "#" + methodInfo.getMethodName() + " 配置Listener: " + listener.getSimpleName()
                        + " 失败, Listener 需要为 Spring Bean");
            }
            builder.addListener(l);
        }

        return builder.build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}