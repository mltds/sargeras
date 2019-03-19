package org.mltds.sargeras.spi.manager.rdbms;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.mltds.sargeras.api.SagaConfig;
import org.mltds.sargeras.api.exception.SagaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author sunyi
 */
public class MybatisDataSourceFactory implements DataSourceFactory {
    /* manager */
    private static final String MANAGER_RDBMS_DATASOURCE = "manager.rdbms.datasource.";
    // 为了缩短代码所以缩写为P
    private static final String P = MANAGER_RDBMS_DATASOURCE;

    private static final Logger logger = LoggerFactory.getLogger(MybatisDataSourceFactory.class);

    private DruidDataSource dataSource;

    @Override
    public void setProperties(Properties props) {

        if (dataSource != null) {
            return;
        }

        synchronized (MybatisDataSourceFactory.class) {

            if (dataSource != null) {
                return;
            }
            try {
                dataSource = new DruidDataSource();

                props.putAll(SagaConfig.getAllProperties());

                dataSource.setUrl(props.getProperty(P + "url"));
                dataSource.setUsername(props.getProperty(P + "username"));
                dataSource.setPassword(props.getProperty(P + "password"));
                dataSource.setFilters(props.getProperty(P + "filters", ""));
                dataSource.setMaxActive(Integer.valueOf(props.getProperty(P + "maxActive", "20")));
                dataSource.setInitialSize(Integer.valueOf(props.getProperty(P + "initialSize", "1")));
                dataSource.setMaxWait(Long.valueOf(props.getProperty(P + "maxWait", "60000")));
                dataSource.setMinIdle(Integer.valueOf(props.getProperty(P + "minIdle", "1")));
                dataSource.setTimeBetweenEvictionRunsMillis(Long.valueOf(props.getProperty(P + "timeBetweenEvictionRunsMillis", "60000")));
                dataSource.setMinEvictableIdleTimeMillis(Long.valueOf(props.getProperty(P + "minEvictableIdleTimeMillis", "300000")));
                dataSource.setTestWhileIdle(Boolean.valueOf(props.getProperty(P + "testWhileIdle", "true")));
                dataSource.setTestOnBorrow(Boolean.valueOf(props.getProperty(P + "testOnBorrow", "false")));
                dataSource.setTestOnReturn(Boolean.valueOf(props.getProperty(P + "testOnReturn", "false")));
                dataSource.setPoolPreparedStatements(Boolean.valueOf(props.getProperty(P + "poolPreparedStatements", "true")));
                dataSource.setMaxOpenPreparedStatements(Integer.valueOf(props.getProperty(P + "maxOpenPreparedStatements", "20")));
                dataSource.setAsyncInit(Boolean.valueOf(props.getProperty(P + "asyncInit", "true")));

                dataSource.init();

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        dataSource.close();
                    }
                });

            } catch (SQLException e) {
                throw new SagaException("Saga RdbmsManager 初始化数据源失败", e);
            }

            logger.debug("Saga RdbmsManager 初始化数据源成功");

        }
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

}
