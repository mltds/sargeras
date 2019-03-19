package org.mltds.sargeras.api;

import java.io.InputStream;
import java.util.Properties;

import org.mltds.sargeras.api.exception.SagaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi
 */
public class SagaConfig {

    public static final String CONFIG_FILE_CUSTOMIZE = "/sargeras.properties";
    public static final String CONFIG_FILE_DEFAULT = "/sargeras/sargeras.default.properties";

    public static final String SPI_FACTORY_PREFIX = "spi.factory.";
    public static final String LISTENER_LOGGER_NAME = "listener.logger.name";

    private static final Logger logger = LoggerFactory.getLogger(SagaConfig.class);
    private static Properties properties = new Properties();

    static {
        load(CONFIG_FILE_DEFAULT, false);
        load(CONFIG_FILE_CUSTOMIZE, true);// 覆盖默认配置
    }

    /**
     * 加载配置项
     */
    public static void load(Properties properties) {
        if (properties == null) {
            return;
        }
        SagaConfig.load(properties);
    }

    /**
     * 加载配置文件
     * 
     * @param file classpath的路径
     * @param ignoreFileNX 是否忽略文件不存在，如果为false且文件不存在会报错。
     */
    public static void load(String file, boolean ignoreFileNX) {
        try {
            Properties p = new Properties();
            InputStream is = SagaConfig.class.getResourceAsStream(file);
            p.load(is);
            is.close();

        } catch (Exception e) {
            if (ignoreFileNX) {
                // do nothing
            } else {
                throw new SagaException("加载配置文件失败:" + file, e);
            }
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static Properties getAllProperties() {
        Properties p = new Properties();
        p.putAll(properties);
        return p;
    }

}
