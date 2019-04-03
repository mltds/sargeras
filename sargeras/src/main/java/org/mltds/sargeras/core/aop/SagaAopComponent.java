package org.mltds.sargeras.core.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.annotation.SagaBizId;
import org.mltds.sargeras.api.annotation.SagaTx;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.exception.expectation.Failure;
import org.mltds.sargeras.api.model.MethodInfo;
import org.mltds.sargeras.api.model.ParamInfo;
import org.mltds.sargeras.api.model.SagaTxRecord;
import org.mltds.sargeras.api.model.SagaTxRecordParam;
import org.mltds.sargeras.core.SagaContext;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.Utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author sunyi.
 */
@Component
public class SagaAopComponent implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    private Serializer serializer;

    @Autowired
    private SagaAopHolder aopHolder;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public org.mltds.sargeras.api.annotation.Saga getSaga(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        org.mltds.sargeras.api.annotation.Saga saga = methodSignature.getMethod().getAnnotation(org.mltds.sargeras.api.annotation.Saga.class);
        return saga;
    }

    public MethodInfo getMethodInfo(JoinPoint joinPoint) {
        MethodInfo methodInfo = new MethodInfo();

        Class<?> cls = joinPoint.getTarget().getClass();
        methodInfo.setCls(cls);
        methodInfo.setClsName(cls.getName());

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        methodInfo.setMethod(method);
        methodInfo.setMethodName(method.getName());

        Class<?>[] parameterTypes = method.getParameterTypes();
        methodInfo.setParameterTypes(parameterTypes);
        methodInfo.setParameterTypesStr(Utils.parameterTypesToString(parameterTypes));

        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        methodInfo.setParameterNames(parameterNames);
        methodInfo.setParameterNamesStr(Utils.arrayToString(parameterNames));

        methodInfo.setParameters(joinPoint.getArgs());

        return methodInfo;

    }

    public MethodInfo getCompensateMethodInfo(MethodInfo executeMethodInfo) {
        Method executeMethod = executeMethodInfo.getMethod();
        SagaTx sagaTx = executeMethod.getAnnotation(SagaTx.class);
        String compensateMethodName = sagaTx.compensate();

        if (StringUtils.isBlank(compensateMethodName)) {
            return null;
        }

        Class<?> cls = executeMethodInfo.getCls();

        Method compensateMethod = findCompensateMethod(cls, compensateMethodName);
        if (compensateMethod == null) {
            throw new SagaException("没有找到 Saga Tx " + executeMethod.toString() + " 的 Compensate Method： " + compensateMethodName);
        }

        MethodInfo compensateMethodInfo = new MethodInfo();

        compensateMethodInfo.setCls(cls);
        compensateMethodInfo.setClsName(cls.getName());

        compensateMethodInfo.setMethod(compensateMethod);
        compensateMethodInfo.setMethodName(compensateMethodName);

        Class<?>[] parameterTypes = compensateMethod.getParameterTypes();
        compensateMethodInfo.setParameterTypes(parameterTypes);
        compensateMethodInfo.setParameterTypesStr(Utils.parameterTypesToString(parameterTypes));

        String[] parameterNames = parameterNameDiscoverer.getParameterNames(compensateMethod);
        compensateMethodInfo.setParameterNames(parameterNames);
        compensateMethodInfo.setParameterNamesStr(Utils.arrayToString(parameterNames));

        return compensateMethodInfo;

    }

    public List<ParamInfo> getParamInfoList(MethodInfo methodInfo) {

        String[] parameterNames = methodInfo.getParameterNames();
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList(parameterNames));

        return getParamInfoList(methodInfo, set);
    }

    /**
     *
     * @param methodInfo
     * @param parameterNames 只获取这些参数名的参数信息
     * @return
     */
    public List<ParamInfo> getParamInfoList(MethodInfo methodInfo, Collection<String> parameterNames) {

        List<ParamInfo> paramInfoList = new ArrayList<>(parameterNames.size());
        String[] parameterNamesArray = methodInfo.getParameterNames();

        for (int i = 0; i < parameterNamesArray.length; i++) {
            String parameterName = parameterNamesArray[i];
            if (parameterNames.contains(parameterName)) {
                ParamInfo paramInfo = new ParamInfo();
                paramInfo.setParameterName(parameterName);
                Class parameterType = methodInfo.getParameterTypes()[i];
                paramInfo.setParameterType(parameterType);
                paramInfo.setParameterTypeStr(parameterType.getName());
                paramInfo.setParameter(methodInfo.getParameters()[i]);
                paramInfo.setParameterByte(serializer.encode(paramInfo.getParameter()));

                paramInfoList.add(paramInfo);
            }
        }

        return paramInfoList;
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
            throw new SagaException(cls + "#" + method.getName() + " 需要传入一个 @SagaBizId");
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
                    doCompensate(txRecord);
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

    public Object doCompensate(SagaTxRecord txRecord) throws Exception {

        SagaContext context = aopHolder.getContext();

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
                Object obj = serializer.decode(param.getParameter(), type);
                args[i] = obj;
            }
        }

        Object bean = applicationContext.getBean(cls);

        return method.invoke(bean, args);

    }

    private Method findCompensateMethod(Class<?> clazz, String compensateMethodName) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(compensateMethodName, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = searchType.getMethods();
            for (Method method : methods) {
                if (compensateMethodName.equals(method.getName())) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

}