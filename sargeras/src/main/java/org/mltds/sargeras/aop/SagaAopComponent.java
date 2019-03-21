package org.mltds.sargeras.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mltds.sargeras.api.*;
import org.mltds.sargeras.api.annotation.SagaBizId;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.expectation.Failure;
import org.mltds.sargeras.api.listener.SagaListener;
import org.mltds.sargeras.api.model.SagaTxRecord;
import org.mltds.sargeras.api.model.SagaTxRecordParam;
import org.mltds.sargeras.api.model.SagaTxRecordResult;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.Utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;

/**
 * @author sunyi.
 */
public class SagaAopComponent implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    private Serializer serializer;

    @Autowired
    private SagaAopHolder aopHolder;

    /**
     * 获取Saga，如果获取不到则构建一个并放到 {@link SagaApplication} 里
     */
    public Saga getSaga(JoinPoint joinPoint) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        org.mltds.sargeras.api.annotation.Saga anno = methodSignature.getMethod().getAnnotation(org.mltds.sargeras.api.annotation.Saga.class);
        String appName = anno.appName();
        String bizName = anno.bizName();

        Saga saga = SagaApplication.getSaga(appName, bizName);
        if (saga != null) {
            return saga;
        }

        Class<?> sagaCls = joinPoint.getTarget().getClass();
        synchronized (sagaCls) {
            saga = SagaApplication.getSaga(appName, bizName);
            if (saga != null) {
                return saga;
            }

            saga = buildSaga(joinPoint);
        }

        return saga;

    }

    /**
     * 构建一个 Saga
     */
    public Saga buildSaga(JoinPoint joinPoint) {
        Class<?> cls = joinPoint.getTarget().getClass();
        String method = joinPoint.getSignature().getName();
        Class[] parameterTypes = new Class[joinPoint.getArgs().length];
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                parameterTypes[i] = args[i].getClass();
            } else {
                parameterTypes[i] = null;
            }
        }

        org.mltds.sargeras.api.annotation.Saga anno =
                ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(org.mltds.sargeras.api.annotation.Saga.class);
        String appName = anno.appName();
        String bizName = anno.bizName();
        int bizTimeout = anno.bizTimeout();
        int lockTimeout = anno.lockTimeout();
        String triggerInterval = anno.triggerInterval();
        Class<? extends SagaListener>[] listeners = anno.listeners();

        SagaBuilder builder = SagaBuilder.newBuilder();
        builder.setAppName(appName)//
                .setBizName(bizName)//
                .setCls(cls)//
                .setMethod(method)//
                .setParamTypes(parameterTypes)//
                .setBizTimeout(bizTimeout)//
                .setLockTimeout(lockTimeout)//
                .setTriggerInterval(triggerInterval)//
        ;

        for (Class<? extends SagaListener> listener : listeners) {
            SagaListener bean = applicationContext.getBean(listener);
            if (bean == null) {
                throw new SagaException(cls + "#" + method + " 配置Listener: " + listener + " 失败, Listener 需要为 Spring Bean");
            }
            builder.addListener(bean);
        }

        return builder.build();
    }

    /**
     * 获取BizId
     */
    public String getBizId(JoinPoint joinPoint) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        int bizIdIndex = -1;

        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            for (Annotation annotation : parameterAnnotation) {
                if (annotation.annotationType().equals(SagaBizId.class)) {
                    bizIdIndex = i;
                }
            }
        }

        if (bizIdIndex < 0) {
            Class<?> cls = joinPoint.getTarget().getClass();
            String methodName = joinPoint.getSignature().getName();
            throw new SagaException(cls + "#" + method + " 需要传入一个 @SagaBizId");
        }

        return joinPoint.getArgs()[bizIdIndex].toString();

    }

    public void compensate() {

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
                    doCompensate(txRecord, context);
                    context.saveTxStatus(txRecord.getId(), SagaTxStatus.COMPENSATE_SUCCESS);
                } catch (Exception e) {
                    if (e instanceof Failure) {
                        context.saveTxStatus(txRecord.getId(), SagaTxStatus.COMPENSATE_FAILURE);
                        context.saveStatus(SagaStatus.COMPENSATE_FAIL);
                    } else {
                        context.saveTxStatus(txRecord.getId(), SagaTxStatus.COMPENSATE_PROCESSING);
                    }
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

    public Object doCompensate(SagaTxRecord txRecord, SagaContext context) throws Exception {

        Long id = txRecord.getId();
        SagaTxStatus txStatus = txRecord.getStatus();

        if (!txStatus.equals(SagaTxStatus.SUCCESS) && !txStatus.equals(SagaTxStatus.FAILURE) && !txStatus.equals(SagaTxStatus.COMPENSATE_PROCESSING)) {
            throw new SagaException("状态不正确无法补偿,SagaTxRecordId: " + txRecord.getId() + ", SagaTxStatus: " + txStatus);
        }

        Class<?> cls = Utils.loadClass(txRecord.getCls());
        String compensateMethod = txRecord.getCompensateMethod();

        Method method = ReflectionUtils.findMethod(cls, compensateMethod);
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

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
                Object obj = serializer.deserialize(param.getParameter(), type);
                args[i] = obj;
            }
        }

        Object bean = applicationContext.getBean(cls);

        return method.invoke(bean, args);

    }

    public Object getFinalResult() {

        SagaContext context = aopHolder.getContext();

        List<SagaTxRecord> txRecordList = context.getTxRecordList();
        SagaTxRecord txRecord = txRecordList.get(txRecordList.size() - 1);
        SagaTxRecordResult txRecordResult = context.getTxRecordResult(txRecord.getId());

        Class cls = Utils.loadClass(txRecordResult.getCls());
        return serializer.deserialize(txRecordResult.getResult(), cls);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}