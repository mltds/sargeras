package org.mltds.sargeras.repository.rdbms;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mltds.sargeras.exception.SagaException;
import org.mltds.sargeras.repository.Repository;
import org.mltds.sargeras.repository.RepositoryFactory;
import org.mltds.sargeras.repository.rdbms.mapper.ContextInfoMapper;
import org.mltds.sargeras.repository.rdbms.mapper.ContextLockMapper;
import org.mltds.sargeras.repository.rdbms.mapper.ContextMapper;

/**
 * 关系型数据库存储方式
 * 
 * @author sunyi
 */
public class RdbmsRepositoryFactory implements RepositoryFactory {

    private static final String MYBATIS_RESOURCE = "sargeras/sargeras-mybatis-config.xml";

    private SqlSessionFactory sqlSessionFactory;

    private RdbmsRepository repository;

    @Override
    public Repository getObject() {

        if (repository != null) {
            return repository;
        }

        synchronized (RdbmsRepositoryFactory.class) {
            if (repository != null) {
                return repository;
            }

            try {
                repository = new RdbmsRepository();

                InputStream inputStream = Resources.getResourceAsStream(MYBATIS_RESOURCE);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

                ContextMapper contextMapper = createMapperProxy(ContextMapper.class);
                repository.setContextMapper(contextMapper);

                ContextInfoMapper contextInfoMapper = createMapperProxy(ContextInfoMapper.class);
                repository.setContextInfoMapper(contextInfoMapper);

                ContextLockMapper contextLockMapper = createMapperProxy(ContextLockMapper.class);
                repository.setContextLockMapper(contextLockMapper);


            } catch (Exception e) {
                throw new SagaException(RdbmsRepository.class.getSimpleName() + "初始化失败!", e);
            }
        }

        return repository;

    }

    @SuppressWarnings("unchecked")
    private <T> T createMapperProxy(Class<T> mapperCls) {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object proxy = Proxy.newProxyInstance(classLoader, new Class[] { mapperCls }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                SqlSession sqlSession = sqlSessionFactory.openSession();

                Object invoke;
                try {
                    T mapper = sqlSession.getMapper(mapperCls);
                    invoke = method.invoke(mapper, args);
                    sqlSession.commit();
                } finally {
                    sqlSession.close();
                }
                return invoke;
            }
        });

        return (T) proxy;

    }

}
