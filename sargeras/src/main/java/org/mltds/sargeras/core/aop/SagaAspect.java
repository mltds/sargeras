package org.mltds.sargeras.core.aop;

import java.util.Date;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaBuilder;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.expectation.Failure;
import org.mltds.sargeras.api.model.MethodInfo;
import org.mltds.sargeras.api.model.ParamInfo;
import org.mltds.sargeras.api.model.SagaRecordResult;
import org.mltds.sargeras.core.SagaApplication;
import org.mltds.sargeras.core.SagaContext;
import org.mltds.sargeras.core.SagaContextFactory;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.NULL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author sunyi.
 */
@Aspect
@Component
public class SagaAspect implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SagaAspect.class);

    @Autowired
    private SagaAopHolder aopHolder;

    @Autowired
    private SagaAopComponent aopComponent;

    @Autowired
    private Serializer serializer;

    @Autowired
    private SagaApplication sagaApplication;

    @Autowired
    private SagaContextFactory sagaContextFactory;

    private ApplicationContext applicationContext;

    @Pointcut("@annotation(org.mltds.sargeras.api.annotation.Saga)")
    public void sagaAspect() {
    }

    private void before(JoinPoint joinPoint) {

        org.mltds.sargeras.api.annotation.Saga sagaAnnotation = aopComponent.getSaga(joinPoint);

        String appName = sagaAnnotation.appName();
        String bizName = sagaAnnotation.bizName();
        String bizId = aopComponent.getBizId(joinPoint);

        SagaContext context = sagaContextFactory.loadContext(appName, bizName, bizId);

        if (context == null) {// 首次执行
            MethodInfo methodInfo = aopComponent.getMethodInfo(joinPoint);
            List<ParamInfo> paramInfoList = aopComponent.getParamInfoList(methodInfo);
            Saga saga = getSaga(joinPoint);
            context = sagaContextFactory.newContext(saga);
            context.firstTrigger(bizId, methodInfo, paramInfoList);
        } else {
            context.trigger();

        }

        aopHolder.setContext(context);

    }

    @Around("sagaAspect()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        before(proceedingJoinPoint);

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

    @AfterReturning(pointcut = "sagaAspect()", returning = "returning")
    public void afterReturning(JoinPoint joinPoint, Object returning) {
        try {
            // 记录 SagaRecord 为成功
            SagaContext context = aopHolder.getContext();
            SagaStatus status = context.getStatus();
            if (status.equals(SagaStatus.EXECUTING)) {

                SagaRecordResult sagaRecordResult = new SagaRecordResult();
                sagaRecordResult.setRecordId(context.getRecordId());
                if (returning == null) {
                    sagaRecordResult.setCls(NULL.class.getName());
                } else {
                    sagaRecordResult.setCls(returning.getClass().getName());
                    sagaRecordResult.setResult(serializer.encode(returning));
                }

                context.saveStatusAndResult(SagaStatus.EXECUTE_SUCC, sagaRecordResult);

            } else if (status.equals(SagaStatus.COMPENSATING)) {
                context.saveStatus(SagaStatus.COMPENSATE_SUCC);
            }
            context.triggerOver();
        } catch (Exception e) {
            logger.error("afterReturning", e); // TODO 优化异常信息
        } finally {
            aopHolder.removeContext();
        }

    }

    @AfterThrowing(pointcut = "sagaAspect()", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) throws Throwable {
        try {
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
        } catch (Exception e) {
            logger.error("afterThrowing", e); // TODO 优化异常信息
        } finally {
            aopHolder.removeContext();
            throw throwable;
        }

    }

    private Saga getSaga(JoinPoint joinPoint) {

        org.mltds.sargeras.api.annotation.Saga anno = aopComponent.getSaga(joinPoint);

        String appName = anno.appName();
        String bizName = anno.bizName();

        Saga saga = sagaApplication.getSaga(appName, bizName);
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
        // Class<? extends SagaListener>[] listeners = anno.listeners();

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

        // for (Class<? extends SagaListener> listener : listeners) {
        // SagaListener l = applicationContext.getBean(listener);
        // if (l == null) {
        // throw new SagaException(methodInfo.getCls().getSimpleName() + "#" + methodInfo.getMethodName() + " 配置Listener: " + listener.getSimpleName()
        // + " 失败, Listener 需要为 Spring Bean");
        // }
        // builder.addListener(l);
        // }

        Saga saga = builder.build();
        sagaApplication.addSaga(saga);
        return saga;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}