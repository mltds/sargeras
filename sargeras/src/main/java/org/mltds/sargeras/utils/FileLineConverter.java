package org.mltds.sargeras.utils;

import ch.qos.logback.classic.pattern.CallerDataConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 用于替换 logback 的 %caller{1}，输出类似log4j的 %F%L的效果，可以看到发起日志请求的行号
 * @author sunyi 
 */
public class FileLineConverter extends CallerDataConverter {

    private static final String EMPTY_STR = "";

    @Override
    public String convert(ILoggingEvent le) {

        StringBuffer buf = new StringBuffer();

        StackTraceElement[] cda = le.getCallerData();
        if (cda == null || cda.length <= 0) {
            return EMPTY_STR;
        }

        String e = cda[0].toString();
        if (!e.contains(".java")) {
            return EMPTY_STR;
        }

        buf.append(e.substring(e.lastIndexOf("(")));

        return buf.toString();
    }
}
