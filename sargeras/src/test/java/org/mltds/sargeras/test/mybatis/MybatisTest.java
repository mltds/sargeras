package org.mltds.sargeras.test.mybatis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mltds.sargeras.spi.manager.rdbms.mapper.ContextInfoMapper;
import org.mltds.sargeras.spi.manager.rdbms.mapper.ContextMapper;
import org.mltds.sargeras.spi.manager.rdbms.model.ContextDO;
import org.mltds.sargeras.spi.manager.rdbms.model.ContextInfoDO;

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
        ContextMapper mapper = sqlSession.getMapper(ContextMapper.class);
        ContextDO contextDO = mapper.selectById(1L);
    }

    @Test
    public void testBlob() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ContextInfoMapper mapper = sqlSession.getMapper(ContextInfoMapper.class);

        Long contextId = 1L;
        String key = UUID.randomUUID().toString().replace("-", "").toUpperCase();

        ContextInfoDO info = new ContextInfoDO();
        info.setContextId(contextId);
        info.setKey(key);
        info.setInfo(this.toString());
        Date now = new Date();
        info.setCreateTime(now);
        info.setModifyTime(now);

        mapper.insert(info);

        info = mapper.selectByKey(contextId, key);

        System.out.println(info.getInfo());

        sqlSession.rollback();

        sqlSession.close();

    }

    @Test
    public void testFindNeedRetryContextList() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ContextMapper mapper = sqlSession.getMapper(ContextMapper.class);
        List<Long> list = mapper.findNeedRetryContextList(new Date(), 100);
        System.out.println(list.size());
    }

}
