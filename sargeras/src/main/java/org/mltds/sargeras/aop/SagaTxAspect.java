package org.mltds.sargeras.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.annotation.SagaTx;
import org.mltds.sargeras.api.exception.expectation.Failure;
import org.mltds.sargeras.api.model.SagaTxRecord;
import org.mltds.sargeras.api.model.SagaTxRecordParam;
import org.mltds.sargeras.api.model.SagaTxRecordResult;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.NULL;
import org.mltds.sargeras.utils.Utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;

/**
 * @author sunyi.
 */
@Aspect
@Component
public class SagaTxAspect implements ApplicationContextAware {

    @Autowired
    private SagaAopHolder aopHolder;

    @Autowired
    private Serializer serializer;

    private DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

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
            SagaTxRecord txRecord = saveTxRecordAndParam(proceedingJoinPoint, context);
            return execute(proceedingJoinPoint, context, txRecord);
        } else {
            SagaTxRecord previousTxRecord = getPreviousTxRecord(proceedingJoinPoint, context);
            SagaTxRecord txRecord = saveTxRecordAndParam(proceedingJoinPoint, context);
            if (previousTxRecord == null) {
                // SagaTx 首次执行，保存执行记录和参数
                return execute(proceedingJoinPoint, context, txRecord);
            } else {
                SagaTxStatus status = previousTxRecord.getStatus();

                if (status.equals(SagaTxStatus.SUCCESS)) {
                    SagaTxRecordResult txRecordResult = context.getTxRecordResult(previousTxRecord.getId());
                    Class resultCls = Utils.loadClass(txRecordResult.getCls());
                    if (NULL.class.equals(resultCls)) {
                        return null;
                    } else {
                        return serializer.deserialize(txRecordResult.getResult(), resultCls);
                    }
                } else {
                    return execute(proceedingJoinPoint, context, txRecord);
                }
            }
        }
    }

    private Object execute(ProceedingJoinPoint proceedingJoinPoint, SagaContext context, SagaTxRecord txRecord) throws Throwable {
        Object result;
        try {
            // 执行
            result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
            // 保存结果
            saveTxRecordResult(txRecord.getRecordId(), txRecord.getId(), result, context);
            return result;
        } catch (Throwable throwable) {
            if (throwable instanceof Failure) {
                context.saveTxStatus(txRecord.getId(), SagaTxStatus.FAILURE);
            } else {
                // 其他异常情况全部视为处理中
                context.saveTxStatus(txRecord.getId(), SagaTxStatus.PROCESSING);
            }
            throw throwable;
        }
    }

    private SagaTxRecord saveTxRecordAndParam(ProceedingJoinPoint proceedingJoinPoint, SagaContext context) {

        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        SagaTx sagaTx = method.getAnnotation(SagaTx.class);
        boolean persistent = sagaTx.paramPersistent();

        SagaTxRecord txRecord = new SagaTxRecord();
        txRecord.setRecordId(context.getRecordId());

        String cls = proceedingJoinPoint.getTarget().getClass().getName();
        txRecord.setCls(cls);

        String methodName = method.getName();
        txRecord.setMethod(methodName);

        String compensate = sagaTx.compensate();
        txRecord.setCompensateMethod(compensate);

        Class<?>[] parameterTypesClass = method.getParameterTypes();
        String parameterTypesStr = Utils.parameterTypesToString(parameterTypesClass);
        txRecord.setParameterTypes(parameterTypesStr);

        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        String parameterNamesStr = Utils.arrayToString(parameterNames);
        txRecord.setParameterNames(parameterNamesStr);

        txRecord.setStatus(SagaTxStatus.PROCESSING);

        List<SagaTxRecordParam> txRecordParamList = null;

        if (persistent && parameterTypesClass != null) {
            int paramCount = parameterTypesClass.length;
            txRecordParamList = new ArrayList<>(paramCount);

            Object[] args = proceedingJoinPoint.getArgs();

            for (int i = 0; i < paramCount; i++) {
                SagaTxRecordParam param = new SagaTxRecordParam();
                param.setRecordId(context.getRecordId());
                param.setCls(parameterTypesClass[i].getName());
                param.setParameterName(parameterNames[i]);
                param.setParameter(serializer.serialize(args[i]));

                txRecordParamList.add(param);
            }

        }

        return context.saveCurrentTxAndParam(txRecord, txRecordParamList);

    }

    private SagaTxRecord getPreviousTxRecord(ProceedingJoinPoint proceedingJoinPoint, SagaContext context) {

        String cls = proceedingJoinPoint.getTarget().getClass().getName();

        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();

        Class<?>[] parameterTypesClass = method.getParameterTypes();
        String parameterTypesStr = Utils.parameterTypesToString(parameterTypesClass);

        List<SagaTxRecord> txRecordList = context.getTxRecordList();
        for (SagaTxRecord txRecord : txRecordList) {
            if (txRecord.getCls().equals(cls) && txRecord.getMethod().equals(methodName) && txRecord.getParameterTypes().equals(parameterTypesStr)) {
                return txRecord;
            }
        }

        return null;

    }

    private SagaTxRecordResult saveTxRecordResult(Long sagaRecordId, Long sagaTxRecord, Object result, SagaContext context) {

        SagaTxRecordResult recordResult = new SagaTxRecordResult();
        recordResult.setRecordId(sagaRecordId);
        recordResult.setTxRecordId(sagaTxRecord);
        if (result == null) {
            recordResult.setCls(NULL.class.getName());
        } else {
            recordResult.setCls(result.getClass().getName());
            recordResult.setResult(serializer.serialize(result));
        }

        return context.saveTxRecordResult(recordResult);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
