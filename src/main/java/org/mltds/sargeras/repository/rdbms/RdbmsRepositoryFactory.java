package org.mltds.sargeras.repository.rdbms;

import org.mltds.sargeras.repository.Repository;
import org.mltds.sargeras.repository.RepositorySagaFactory;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.SQLException;

/**
 * @author sunyi 2019/2/20.
 */
public class RdbmsRepositoryFactory implements RepositorySagaFactory {

    private DruidDataSource dataSource = new DruidDataSource();


    @Override
    public Repository getObject() {
        return null;
    }

    private void initDatasource() throws SQLException {





    }

}
