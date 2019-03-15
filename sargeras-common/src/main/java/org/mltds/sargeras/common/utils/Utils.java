package org.mltds.sargeras.common.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.mltds.sargeras.common.exception.SagaException;

/**
 * @author sunyi
 */
public class Utils {

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

    /**
     * 获取本机IPv4地址
     */
    public static String getLocalIPv4() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        if (ip != null && !ip.equals("127.0.0.1")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SagaException("获取本机IP失败", e);
        }
        return null;
    }

}
