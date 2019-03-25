package org.mltds.sargeras.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.exception.expectation.Failure;
import org.mltds.sargeras.api.model.*;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.NULL;
import org.mltds.sargeras.utils.Utils;
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

        if (context.isFirstTrigger()) {
            // Saga 首次执行，默认直接保存执行记录和参数
            SagaTxRecord txRecord = saveTxRecordAndParam(proceedingJoinPoint);
            // 然后执行
            return doExecute(proceedingJoinPoint, txRecord);
        } else {
            // 先判断下之前有没有执行过
            SagaTxRecord previousTxRecord = getPreviousTxRecord(proceedingJoinPoint);

            if (previousTxRecord == null) {
                // 没执行过类同首次执行，保存记录和参数
                SagaTxRecord txRecord = saveTxRecordAndParam(proceedingJoinPoint);
                // 开始执行
                return doExecute(proceedingJoinPoint, txRecord);
            } else {
                // 之前执行过，则获状态
                SagaTxStatus status = previousTxRecord.getStatus();

                if (status.equals(SagaTxStatus.SUCCESS)) {
                    // 之前执行成功过，则获取结果
                    return getTxRecordResult(previousTxRecord.getId());
                } else {
                    // 如果之前执行没有成功，需要再次执行
                    return doExecute(proceedingJoinPoint, previousTxRecord);
                }
            }
        }
    }

    private SagaTxRecord saveTxRecordAndParam(ProceedingJoinPoint proceedingJoinPoint) {
        SagaContext context = aopHolder.getContext();

        MethodInfo methodInfo = aopComponent.getMethodInfo(proceedingJoinPoint);

        SagaTxRecord txRecord = new SagaTxRecord();
        txRecord.setRecordId(context.getRecordId());

        String clsName = methodInfo.getClsName();
        txRecord.setCls(clsName);

        String methodName = methodInfo.getMethodName();
        txRecord.setMethod(methodName);

        MethodInfo compensateMethodInfo = aopComponent.getCompensateMethodInfo(methodInfo);
        if (compensateMethodInfo != null) {
            txRecord.setCompensateMethod(compensateMethodInfo.getMethodName());
        }

        String parameterTypesStr = methodInfo.getParameterTypesStr();
        txRecord.setParameterTypes(parameterTypesStr);

        txRecord.setStatus(SagaTxStatus.PROCESSING);

        List<SagaTxRecordParam> txRecordParamList = null;

        Object[] parameters = methodInfo.getParameters();
        if (ArrayUtils.isNotEmpty(parameters) && compensateMethodInfo != null) {

            String[] parameterNames = compensateMethodInfo.getParameterNames();
            Set<String> paramNameSet = new HashSet<>();
            CollectionUtils.addAll(paramNameSet, parameterNames);

            List<ParamInfo> paramInfoList = aopComponent.getParamInfoList(methodInfo, paramNameSet);

            txRecordParamList = new ArrayList<>(paramInfoList.size());
            for (ParamInfo paramInfo : paramInfoList) {
                SagaTxRecordParam param = new SagaTxRecordParam();
                param.setRecordId(context.getRecordId());
                param.setParameterType(paramInfo.getParameterTypeStr());
                param.setParameterName(paramInfo.getParameterName());
                param.setParameter(paramInfo.getParameterByte());

                txRecordParamList.add(param);
            }
        }

        return context.saveCurrentTxAndParam(txRecord, txRecordParamList);
    }

    public Object doExecute(ProceedingJoinPoint proceedingJoinPoint, SagaTxRecord txRecord) throws Throwable {

        SagaContext context = aopHolder.getContext();

        Object result;
        try {
            // 执行
            result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
            // 保存结果
            saveTxRecordResult(txRecord.getRecordId(), txRecord.getId(), result);
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

    public SagaTxRecord getPreviousTxRecord(ProceedingJoinPoint proceedingJoinPoint) {

        SagaContext context = aopHolder.getContext();

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

    public void saveTxRecordResult(Long sagaRecordId, Long sagaTxRecord, Object result) {

        SagaContext context = aopHolder.getContext();

        SagaTxRecordResult recordResult = new SagaTxRecordResult();
        recordResult.setRecordId(sagaRecordId);
        recordResult.setTxRecordId(sagaTxRecord);
        if (result == null) {
            recordResult.setCls(NULL.class.getName());
        } else {
            recordResult.setCls(result.getClass().getName());
            recordResult.setResult(serializer.serialize(result));
        }

        context.saveTxRecordSuccAndResult(recordResult);

    }

    public Object getTxRecordResult(Long sagaTxRecord) {

        SagaContext context = aopHolder.getContext();

        SagaTxRecordResult txRecordResult = context.getTxRecordResult(sagaTxRecord);
        Class resultCls = Utils.loadClass(txRecordResult.getCls());
        if (NULL.class.equals(resultCls)) {
            return null;
        } else {
            return serializer.deserialize(txRecordResult.getResult(), resultCls);
        }
    }

}
