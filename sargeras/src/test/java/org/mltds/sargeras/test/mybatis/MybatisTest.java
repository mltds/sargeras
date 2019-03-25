package org.mltds.sargeras.test.mybatis;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mltds.sargeras.spi.manager.rdbms.mapper.SagaRecordMapper;

/**
 * @author sunyi
 */
public class MybatisTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws IOException {
        String resource = "sargeras/sargeras-mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testConn() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SagaRecordMapper mapper = sqlSession.getMapper(SagaRecordMapper.class);
    }

}
