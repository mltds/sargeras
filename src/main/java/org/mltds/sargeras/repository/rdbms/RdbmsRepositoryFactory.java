package org.mltds.sargeras.repository.rdbms;

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

    private Repository repository;
    private SqlSessionFactory sqlSessionFactory;
    private ThreadLocal<SqlSession> sqlSessionThreadLocal = new ThreadLocal<>();

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
                RdbmsRepository repository = new RdbmsRepository();

                InputStream inputStream = Resources.getResourceAsStream(MYBATIS_RESOURCE);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

                ContextMapper contextMapper = createMapperProxy(ContextMapper.class);
                repository.setContextMapper(contextMapper);

                ContextInfoMapper contextInfoMapper = createMapperProxy(ContextInfoMapper.class);
                repository.setContextInfoMapper(contextInfoMapper);

                ContextLockMapper contextLockMapper = createMapperProxy(ContextLockMapper.class);
                repository.setContextLockMapper(contextLockMapper);

                Repository repositoryProxy = createRepositoryProxy(repository);// 为了支持事务

                this.repository = repositoryProxy;

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

                SqlSession sqlSession = sqlSessionThreadLocal.get();
                boolean inTransaction = sqlSession != null;

                if (!inTransaction) {
                    sqlSession = sqlSessionFactory.openSession();
                }

                Object invoke;
                try {
                    T mapper = sqlSession.getMapper(mapperCls);
                    invoke = method.invoke(mapper, args);
                    if (!inTransaction) {
                        sqlSession.commit();
                    }
                } catch (Exception e) {
                    if (!inTransaction) {
                        sqlSession.rollback();
                    }
                    throw e;
                } finally {
                    if (!inTransaction) {
                        sqlSession.close();
                    }
                }
                return invoke;
            }
        });

        return (T) proxy;

    }

    private Repository createRepositoryProxy(RdbmsRepository repository) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object proxy = Proxy.newProxyInstance(classLoader, new Class[] { Repository.class }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Object result = null;

                Transaction transaction = method.getAnnotation(Transaction.class);
                if (transaction != null) {
                    SqlSession sqlSession = sqlSessionThreadLocal.get();
                    if (sqlSession == null) {
                        sqlSession = sqlSessionFactory.openSession();
                        sqlSessionThreadLocal.set(sqlSession);
                    }

                    try {
                        result = method.invoke(repository, args);
                        sqlSession.commit();
                    } catch (Exception e) {
                        sqlSession.rollback();
                        throw e;
                    } finally {
                        sqlSessionThreadLocal.remove();
                        sqlSession.close();
                    }
                } else {
                    result = method.invoke(repository, args);
                }

                return result;
            }
        });

        return (Repository) proxy;
    }

}
