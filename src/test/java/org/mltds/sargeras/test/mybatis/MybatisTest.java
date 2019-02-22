package org.mltds.sargeras.test.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mltds.sargeras.SagaContext;
import org.mltds.sargeras.repository.rdbms.mapper.ContextMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author sunyi 2019/2/22.
 */
public class MybatisTest {


    public static void main(String[] args) throws IOException {
        String resource = "sargeras/sargeras-mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession sqlSession = sqlSessionFactory.openSession();

        ContextMapper mapper = sqlSession.getMapper(ContextMapper.class);

        SagaContext context = mapper.selectById(1L);


    }


}
