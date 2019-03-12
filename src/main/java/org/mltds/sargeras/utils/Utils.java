package org.mltds.sargeras.utils;

/**
 * @author sunyi
 */
public class Utils {

    /**
     * 根据给定的 class name 加载一个Class, 如果没有则返回 null.
     */
    public static Class loadClass(String className) {
        try {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Class<?> cls = Class.forName(className, false, ccl);
            return cls;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
