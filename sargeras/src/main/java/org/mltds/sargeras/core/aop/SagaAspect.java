package org.mltds.sargeras.core.aop;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.SagaTxFailure;
import org.mltds.sargeras.api.SagaTxProcessing;
import org.mltds.sargeras.api.model.*;
import org.mltds.sargeras.core.SagaApplication;
import org.mltds.sargeras.core.SagaContext;
import org.mltds.sargeras.core.SagaContextFactory;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.NULL;
import org.mltds.sargeras.utils.Utils;
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

            Saga saga = sagaApplication.getSaga(appName, bizName);

            MethodInfo methodInfo = aopComponent.getMethodInfo(joinPoint);
            List<ParamInfo> paramInfoList = aopComponent.getParamInfoList(methodInfo);

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
            compensate();
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
        } finally {
            aopHolder.removeContext();
        }

    }

    @AfterThrowing(pointcut = "sagaAspect()", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) throws Throwable {
        try {
            SagaContext context = aopHolder.getContext();
            SagaStatus status = context.getStatus();

            if (throwable instanceof SagaTxFailure) {
                if (status.equals(SagaStatus.EXECUTING)) {
                    compensate();
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
        } finally {
            aopHolder.removeContext();
            throw throwable;
        }

    }

    private void compensate() throws Throwable {

        SagaContext context = aopHolder.getContext();

        SagaStatus status = context.getStatus();
        if (status.equals(SagaStatus.EXECUTING)) {
            context.saveStatus(SagaStatus.COMPENSATING);
        }

        List<SagaTxRecord> txRecordList = context.getTxRecordList();
        for (int i = txRecordList.size() - 1; i >= 0; i--) {
            SagaTxRecord txRecord = txRecordList.get(i);
            SagaTxStatus txStatus = txRecord.getStatus();

            if (txStatus.equals(SagaTxStatus.SUCCESS) || txStatus.equals(SagaTxStatus.FAILURE) || txStatus.equals(SagaTxStatus.COMPENSATE_PROCESSING)) {
                try {

                    doCompensate(txRecord);

                } catch (Throwable e) {
                    if (e instanceof SagaTxFailure) {
                        context.saveStatus(SagaStatus.COMPENSATE_FAIL);
                    } else if (e instanceof SagaTxProcessing) {
                        // 补偿处理中，不需要做什么
                    } else {
                        // 其他异常均视为处理中，不能将系统异常当做业务失败。
                        logger.warn("补偿过程中发生异常，Saga Tx Record ID: " + txRecord.getId(), e);
                    }
                    throw e;
                }
            } else if (txStatus.equals(SagaTxStatus.COMPENSATE_FAILURE)) {
                context.saveStatus(SagaStatus.COMPENSATE_FAIL);
            } else if (txStatus.equals(SagaTxStatus.COMPENSATE_SUCCESS)) {
                // 补偿过了，不再补偿
            } else {
                throw new SagaException("补偿过程中发现不合理的状态,SagaTxRecordId: " + txRecord.getId() + ", SagaTxStatus: " + txStatus);
            }
        }

    }

    private Object doCompensate(SagaTxRecord txRecord) throws Exception {

        SagaContext context = aopHolder.getContext();

        Long id = txRecord.getId();
        SagaTxStatus txStatus = txRecord.getStatus();

        if (!txStatus.equals(SagaTxStatus.SUCCESS) && !txStatus.equals(SagaTxStatus.FAILURE) && !txStatus.equals(SagaTxStatus.COMPENSATE_PROCESSING)) {
            throw new SagaException("状态不正确无法补偿,SagaTxRecordId: " + id + ", SagaTxStatus: " + txStatus);
        }

        Class<?> cls = Utils.loadClass(txRecord.getCls());
        String compensateMethodName = txRecord.getCompensateMethod();
        if (StringUtils.isBlank(compensateMethodName)) {
            return null;
        }

        MethodInfo compensateMethodInfo = aopComponent.getCompensateMethodInfo(cls, compensateMethodName);

        if (compensateMethodInfo == null) {
            throw new SagaException("没有找到 Saga Tx " + txRecord.getCls() + "#" + txRecord.getMethod() + " 的 Compensate 方法： " + compensateMethodName);
        }

        Class<?>[] parameterTypes = compensateMethodInfo.getParameterTypes();
        String[] parameterNames = compensateMethodInfo.getParameterNames();

        List<SagaTxRecordParam> paramList = context.getTxRecordParam(txRecord.getId());
        Map<String, SagaTxRecordParam> map = new HashMap<>(paramList.size());
        for (SagaTxRecordParam param : paramList) {
            map.put(param.getParameterName(), param);
        }

        Object args[] = new Object[parameterNames.length];
        for (int i = 0; i < args.length; i++) {
            String name = parameterNames[i];
            Class<?> type = parameterTypes[i];

            SagaTxRecordParam param = map.get(name);
            String parameterName = param.getParameterName();
            String parameterType = param.getParameterType();

            if (name.equals(parameterName) && type.getName().equals(parameterType)) {
                Object obj = serializer.decode(param.getParameter(), type);
                args[i] = obj;
            }
        }

        Object bean = applicationContext.getBean(cls);
        Method compensateMethod = compensateMethodInfo.getMethod();

        try {
            // 执行补偿
            Object result = compensateMethod.invoke(bean, args);
            context.saveTxStatus(txRecord.getId(), SagaTxStatus.COMPENSATE_SUCCESS);
            return result;

        } catch (Throwable e) {
            if (e instanceof SagaTxFailure) {
                // 补偿失败
                context.saveTxStatus(txRecord.getId(), SagaTxStatus.COMPENSATE_FAILURE);
            } else if (e instanceof SagaTxProcessing) {
                // 补偿中
                context.saveTxStatus(txRecord.getId(), SagaTxStatus.COMPENSATE_PROCESSING);
            } else {
                // 其他异常都视为处理中
                context.saveTxStatus(txRecord.getId(), SagaTxStatus.COMPENSATE_PROCESSING);
                logger.warn("补偿过程中发生异常，Saga Tx Record ID: " + txRecord.getId(), e);
            }
            throw e;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}