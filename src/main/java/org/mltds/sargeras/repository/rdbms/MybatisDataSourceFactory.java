package org.mltds.sargeras.repository.rdbms;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.mltds.sargeras.component.SagaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author sunyi 2019/2/21.
 */
public class MybatisDataSourceFactory implements DataSourceFactory {

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
                dataSource.setUrl(props.getProperty("url", "${jdbc_url}"));
                dataSource.setUsername(props.getProperty("username", "${jdbc_user}"));
                dataSource.setPassword(props.getProperty("password", "${jdbc_password}"));
                dataSource.setFilters(props.getProperty("filters", "stat"));
                dataSource.setMaxActive(Integer.valueOf(props.getProperty("maxActive", "20")));
                dataSource.setInitialSize(Integer.valueOf(props.getProperty("initialSize", "1")));
                dataSource.setMaxWait(Long.valueOf(props.getProperty("maxWait", "60000")));
                dataSource.setMinIdle(Integer.valueOf(props.getProperty("minIdle", "1")));
                dataSource.setTimeBetweenEvictionRunsMillis(Long.valueOf(props.getProperty("timeBetweenEvictionRunsMillis", "60000")));
                dataSource.setMinEvictableIdleTimeMillis(Long.valueOf(props.getProperty("minEvictableIdleTimeMillis", "300000")));
                dataSource.setTestWhileIdle(Boolean.valueOf(props.getProperty("testWhileIdle", "true")));
                dataSource.setTestOnBorrow(Boolean.valueOf(props.getProperty("testOnBorrow", "false")));
                dataSource.setTestOnReturn(Boolean.valueOf(props.getProperty("testOnReturn", "false")));
                dataSource.setPoolPreparedStatements(Boolean.valueOf(props.getProperty("poolPreparedStatements", "true")));
                dataSource.setMaxOpenPreparedStatements(Integer.valueOf(props.getProperty("maxOpenPreparedStatements", "20")));
                dataSource.setAsyncInit(Boolean.valueOf(props.getProperty("asyncInit", "true")));

                dataSource.init();


            } catch (SQLException e) {
                throw new SagaException("Saga RdbmsRepository 初始化数据源失败", e);
            }

            logger.info("Saga RdbmsRepository 初始化数据源成功");

        }
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;

    }

}
