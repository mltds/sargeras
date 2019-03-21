package org.mltds.sargeras.utils;

/**
 * @author sunyi
 */
public class Utils {

    public static final String STRING_REGEX = ",";

    /**
     * 根据给定的 class name 加载一个Class, 如果没有则返回 null.
     */
    public static Class loadClass(String className) {
        try {

            if (className == null || className.length() <= 0) {
                return null;
            }

            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Class<?> cls = Class.forName(className, false, ccl);
            return cls;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static String parameterTypesToString(Class<?>[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder(parameterTypes[0].getName());
        for (int i = 1; i < parameterTypes.length; i++) {
            builder.append(STRING_REGEX);
            builder.append(parameterTypes[i].getName());
        }

        return builder.toString();
    }

    public static Class<?>[] parameterTypesToClass(String parameterTypes) {
        if (parameterTypes == null || parameterTypes.length() == 0) {
            return null;
        }

        String[] array = parameterTypes.split(STRING_REGEX);
        Class<?>[] clsArray = new Class[array.length];

        for (int i = 0; i < array.length; i++) {
            clsArray[i] = loadClass(array[i]);
        }

        return clsArray;
    }

    public static String arrayToString(Object[] objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder(objects[0].toString());
        for (int i = 1; i < objects.length; i++) {
            builder.append(STRING_REGEX);
            builder.append(objects[i].toString());
        }

        return builder.toString();

    }

    public static String[] stringToArray(String string) {
        return string.split(STRING_REGEX);
    }

}
