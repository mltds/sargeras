package org.mltds.sargeras.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.listener.SagaListener;

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
    public static final String TRIGGERINTERVAL_SPLIT_REGEX = ",";

    private String appName;
    private String bizName;

    private Class<?> cls;
    private String method;
    private Class[] parameterTypes;

    private int lockTimeout;
    private int bizTimeout;
    private int[] triggerInterval;

    private List<SagaListener> listenerList = new ArrayList<>();

    Saga() {

    }

    private static int[] conversionTriggerInterval(String triggerInterval) {
        if (StringUtils.isBlank(triggerInterval)) {
            throw new SagaException("错误格式的 triggerInterval：" + triggerInterval);
        }

        String[] split = triggerInterval.split(TRIGGERINTERVAL_SPLIT_REGEX);

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

    public Class<?> getCls() {
        return cls;
    }

    void setCls(Class<?> cls) {
        this.cls = cls;
    }

    public String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    public String getParameterTypes() {
        return Arrays.toString(parameterTypes);
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

    void setTriggerInterval(String triggerInterval) {
        setTriggerInterval(conversionTriggerInterval(triggerInterval));
    }

    void setTriggerInterval(int[] triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    public List<SagaListener> getListenerList() {
        return Collections.unmodifiableList(listenerList);
    }

    public void addListener(SagaListener listenerList) {
        this.listenerList.add(listenerList);
    }

    public String getKeyName() {
        return Saga.getKeyName(appName, bizName);
    }

    @Override
    public String toString() {
        return "Saga{" + "appName='" + appName + '\'' + ", bizName='" + bizName + '\'' + ", cls=" + cls + ", method='" + method + '\'' + ", parameterTypes="
                + Arrays.toString(parameterTypes) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Saga))
            return false;

        Saga saga = (Saga) o;

        if (getLockTimeout() != saga.getLockTimeout())
            return false;
        if (getBizTimeout() != saga.getBizTimeout())
            return false;
        if (getAppName() != null ? !getAppName().equals(saga.getAppName()) : saga.getAppName() != null)
            return false;
        if (getBizName() != null ? !getBizName().equals(saga.getBizName()) : saga.getBizName() != null)
            return false;
        if (getCls() != null ? !getCls().equals(saga.getCls()) : saga.getCls() != null)
            return false;
        if (getMethod() != null ? !getMethod().equals(saga.getMethod()) : saga.getMethod() != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (getParameterTypes() != null ? !getParameterTypes().equals(saga.getParameterTypes()) : saga.getParameterTypes() != null)
            return false;
        if (!Arrays.equals(getTriggerInterval(), saga.getTriggerInterval()))
            return false;
        return getListenerList() != null ? getListenerList().equals(saga.getListenerList()) : saga.getListenerList() == null;
    }

    @Override
    public int hashCode() {
        int result = getAppName() != null ? getAppName().hashCode() : 0;
        result = 31 * result + (getBizName() != null ? getBizName().hashCode() : 0);
        result = 31 * result + (getCls() != null ? getCls().hashCode() : 0);
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getParameterTypes() != null ? getParameterTypes().hashCode() : 0);
        result = 31 * result + getLockTimeout();
        result = 31 * result + getBizTimeout();
        result = 31 * result + Arrays.hashCode(getTriggerInterval());
        result = 31 * result + (getListenerList() != null ? getListenerList().hashCode() : 0);
        return result;
    }
}
