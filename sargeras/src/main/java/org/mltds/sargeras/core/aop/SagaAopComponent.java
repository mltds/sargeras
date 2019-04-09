package org.mltds.sargeras.core.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mltds.sargeras.api.annotation.NonPersistent;
import org.mltds.sargeras.api.annotation.SagaBizId;
import org.mltds.sargeras.api.annotation.SagaTx;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.model.MethodInfo;
import org.mltds.sargeras.api.model.ParamInfo;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.mltds.sargeras.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author sunyi.
 */
@Component
public class SagaAopComponent {

    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    private Serializer serializer;

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

    public MethodInfo getMethodInfo(Method method) {
        MethodInfo methodInfo = new MethodInfo();

        Class<?> cls = method.getDeclaringClass();
        methodInfo.setCls(cls);
        methodInfo.setClsName(cls.getName());

        methodInfo.setMethod(method);
        methodInfo.setMethodName(method.getName());

        Class<?>[] parameterTypes = method.getParameterTypes();
        methodInfo.setParameterTypes(parameterTypes);
        methodInfo.setParameterTypesStr(Utils.parameterTypesToString(parameterTypes));

        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        methodInfo.setParameterNames(parameterNames);
        methodInfo.setParameterNamesStr(Utils.arrayToString(parameterNames));

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

        return getCompensateMethodInfo(cls, compensateMethodName);
    }

    public MethodInfo getCompensateMethodInfo(Class<?> cls, String compensateMethodName) {

        Method compensateMethod = getCompensateMethod(cls, compensateMethodName);
        if (compensateMethod == null) {
            throw new SagaException("没有找到 Saga Tx 的 Compensate 方法： " + compensateMethodName);
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
     * @param parameterNames 只获取这些参数名的参数信息
     */
    public List<ParamInfo> getParamInfoList(MethodInfo methodInfo, Collection<String> parameterNames) {

        List<ParamInfo> paramInfoList = new ArrayList<>(parameterNames.size());
        String[] parameterNamesArray = methodInfo.getParameterNames();

        Annotation[][] parameterAnnotations = methodInfo.getMethod().getParameterAnnotations();

        for (int i = 0; i < parameterNamesArray.length; i++) {
            String parameterName = parameterNamesArray[i];
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            if (!parameterNames.contains(parameterName)) {
                continue;
            }

            // 如果参数上面标注了 @NonPersistent ，则这个参数不会被持久化
            boolean nonPersistent = false;
            if (parameterAnnotation != null && parameterAnnotation.length > 0) {
                for (Annotation anno : parameterAnnotation) {
                    if (anno.annotationType().equals(NonPersistent.class)) {
                        nonPersistent = true;
                        break;
                    }
                }
            }

            if (nonPersistent) {
                continue;
            }

            ParamInfo paramInfo = new ParamInfo();
            paramInfo.setParameterName(parameterName);
            Class parameterType = methodInfo.getParameterTypes()[i];
            paramInfo.setParameterType(parameterType);
            paramInfo.setParameterTypeStr(parameterType.getName());
            paramInfo.setParameter(methodInfo.getParameters()[i]);
            paramInfo.setParameterByte(serializer.encode(paramInfo.getParameter()));

            paramInfoList.add(paramInfo);
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

    public Method getCompensateMethod(Class<?> clazz, String compensateMethodName) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(compensateMethodName, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = searchType.getMethods();
            for (Method method : methods) {
                if (compensateMethodName.equals(method.getName())) {
                    // 只选择第一个，因为在注解上比较难表明 Compensate 方法的参数有哪些，所以只靠名称来获取 Compensate 方法
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

}