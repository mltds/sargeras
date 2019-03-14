package org.mltds.sargeras.api;

import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.mltds.sargeras.exception.SagaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi
 */
public class SagaConfig {

    public static final String CONFIG_FILE = "/sargeras.properties";
    public static final String CONFIG_FILE_DEFAULT = "/sargeras/sargeras.default.properties";

    public static final String FACTORY_PREFIX = "factory.";
    public static final String REPOSITORY_RDBMS_DATASOURCE = "repository.rdbms.datasource.";
    public static final String LISTENER_LOGGER_NAME = "listener.logger.name";

    /* pollretry */
    public static final String POLLRETRY_PREFIX = "pollretry.";
    public static final String POLLRETRY_NTHREADS = POLLRETRY_PREFIX + "nthreads";
    public static final String POLLRETRY_LIMIT = POLLRETRY_PREFIX + "limit";
    public static final String POLLRETRY_INTERVAL = POLLRETRY_PREFIX + "interval";

    private static final Logger logger = LoggerFactory.getLogger(SagaConfig.class);
    private static Properties prop = new Properties();

    static {
        load(prop, CONFIG_FILE_DEFAULT, false);
        load(prop, CONFIG_FILE, true);// 覆盖默认配置

        if (logger.isDebugEnabled()) {
            logger.debug("加载配置信息:");
            Set<Object> set = new TreeSet<>();
            set.addAll(prop.keySet());
            for (Object k : set) {
                String key = k.toString();
                logger.debug(key + " --- " + prop.getProperty(key));
            }
        }

    }

    private static void load(Properties prop, String file, boolean ignoreFileNX) {
        try {
            Properties p = new Properties();
            InputStream is = SagaConfig.class.getResourceAsStream(file);
            p.load(is);
            is.close();
            prop.putAll(p);
        } catch (Exception e) {
            if (ignoreFileNX) {
                // do nothing
            } else {
                throw new SagaException("加载配置文件失败:" + file, e);
            }
        }
    }

    public static String getProperty(String key) {
        return prop.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }

    public static Properties getAllProperties() {
        Properties p = new Properties();
        p.putAll(prop);
        return p;
    }

}
