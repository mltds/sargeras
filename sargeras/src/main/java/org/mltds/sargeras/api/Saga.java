package org.mltds.sargeras.api;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.mltds.sargeras.api.exception.SagaException;

/**
 * Saga 代表着一个长事务（LLT,long live transaction），由多个小事务（Tx）有序组成。<br/>
 * 利用 {@link SagaBuilder} 构建，被构建后不可更改，线程安全。
 *
 * @author sunyi
 */
public class Saga {

    /**
     * 默认每次执行时占有锁的最长时间，100秒。
     */
    public static final int DEFAULT_LOCK_TIMEOUT = 100;
    /**
     * 默认每笔业务的超时时间，1天。
     */
    public static final int DEFAULT_BIZ_TIMEOUT = 60 * 60 * 24;

    /**
     * 默认触发间隔,<br/>
     * 枚举的属性不支持数组，因为数组的值是可变对象，所以需要一个 String 常量。 attribute value must be constant
     */
    public static final String DEFAULT_TRIGGER_INTERVAL_STR = "1,2,4,8,16,32,64,128";
    public static final String TRIGGER_INTERVAL_SPLIT_REGEX = ",";

    private String appName;
    private String bizName;

    private Class<?> beanClass;
    private String beanName;
    private Method method;
    private Class[] parameterTypes;

    private int lockTimeout;
    private int bizTimeout;
    private int[] triggerInterval;

    Saga() {

    }

    private static int[] conversionTriggerInterval(String triggerInterval) {
        if (StringUtils.isBlank(triggerInterval)) {
            throw new SagaException("错误格式的 triggerInterval：" + triggerInterval);
        }

        String[] split = triggerInterval.split(TRIGGER_INTERVAL_SPLIT_REGEX);

        if (split.length == 0) {
            throw new SagaException("错误格式的 triggerInterval：" + triggerInterval);
        }

        int[] intArray = new int[split.length];

        try {
            for (int i = 0; i < split.length; i++) {
                intArray[i] = Integer.valueOf(split[i]);
            }
        } catch (Exception e) {
            throw new SagaException("错误格式的 triggerInterval：" + triggerInterval, e);
        }

        return intArray;

    }

    public static String getKeyName(String appName, String bizName) {
        return appName + "-" + bizName;
    }

    public String getAppName() {
        return appName;
    }

    void setAppName(String appName) {
        this.appName = appName;
    }

    public String getBizName() {
        return bizName;
    }

    void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Method getMethod() {
        return method;
    }

    void setMethod(Method method) {
        this.method = method;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public int getLockTimeout() {
        return lockTimeout;
    }

    void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public int getBizTimeout() {
        return bizTimeout;
    }

    void setBizTimeout(int bizTimeout) {
        this.bizTimeout = bizTimeout;
    }

    public int[] getTriggerInterval() {
        return triggerInterval;
    }

    void setTriggerInterval(int[] triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    void setTriggerInterval(String triggerInterval) {
        setTriggerInterval(conversionTriggerInterval(triggerInterval));
    }

    public String getKeyName() {
        return Saga.getKeyName(appName, bizName);
    }

    @Override
    public String toString() {
        return "Saga{" +
                "appName='" + appName + '\'' +
                ", bizName='" + bizName + '\'' +
                ", beanClass=" + beanClass +
                ", beanName='" + beanName + '\'' +
                ", method=" + method +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Saga)) return false;

        Saga saga = (Saga) o;

        if (getAppName() != null ? !getAppName().equals(saga.getAppName()) : saga.getAppName() != null) return false;
        if (getBizName() != null ? !getBizName().equals(saga.getBizName()) : saga.getBizName() != null) return false;
        if (getBeanClass() != null ? !getBeanClass().equals(saga.getBeanClass()) : saga.getBeanClass() != null) return false;
        if (getBeanName() != null ? !getBeanName().equals(saga.getBeanName()) : saga.getBeanName() != null)
            return false;
        if (getMethod() != null ? !getMethod().equals(saga.getMethod()) : saga.getMethod() != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getParameterTypes(), saga.getParameterTypes());
    }

    @Override
    public int hashCode() {
        int result = getAppName() != null ? getAppName().hashCode() : 0;
        result = 31 * result + (getBizName() != null ? getBizName().hashCode() : 0);
        result = 31 * result + (getBeanClass() != null ? getBeanClass().hashCode() : 0);
        result = 31 * result + (getBeanName() != null ? getBeanName().hashCode() : 0);
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + Arrays.hashCode(getParameterTypes());
        return result;
    }
}
