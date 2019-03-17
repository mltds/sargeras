package org.mltds.sargeras.spi.manager.rdbms;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.spi.manager.Manager;
import org.mltds.sargeras.spi.manager.ManagerFactory;
import org.mltds.sargeras.spi.manager.rdbms.mapper.ContextInfoMapper;
import org.mltds.sargeras.spi.manager.rdbms.mapper.ContextLockMapper;
import org.mltds.sargeras.spi.manager.rdbms.mapper.ContextMapper;

/**
 * 关系型数据库存储方式
 * 
 * @author sunyi
 */
public class RdbmsManagerFactory implements ManagerFactory {

    private static final String MYBATIS_RESOURCE = "sargeras/sargeras-mybatis-config.xml";

    private Manager manager;
    private SqlSessionFactory sqlSessionFactory;
    private ThreadLocal<SqlSession> sqlSessionThreadLocal = new ThreadLocal<>();

    @Override
    public Manager getObject() {

        if (manager != null) {
            return manager;
        }

        synchronized (RdbmsManagerFactory.class) {
            if (manager != null) {
                return manager;
            }

            try {
                RdbmsManager manager = new RdbmsManager();

                InputStream inputStream = Resources.getResourceAsStream(MYBATIS_RESOURCE);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

                ContextMapper contextMapper = createMapperProxy(ContextMapper.class);
                manager.setContextMapper(contextMapper);

                ContextInfoMapper contextInfoMapper = createMapperProxy(ContextInfoMapper.class);
                manager.setContextInfoMapper(contextInfoMapper);

                ContextLockMapper contextLockMapper = createMapperProxy(ContextLockMapper.class);
                manager.setContextLockMapper(contextLockMapper);

                Manager managerProxy = createRdbmsManagerProxy(manager);// 为了支持事务

                this.manager = managerProxy;

            } catch (Exception e) {
                throw new SagaException(RdbmsManager.class.getSimpleName() + "初始化失败!", e);
            }
        }

        return manager;

    }

    @SuppressWarnings("unchecked")
    private <T> T createMapperProxy(final Class<T> mapperCls) {

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

    private Manager createRdbmsManagerProxy(final RdbmsManager manager) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object proxy = Proxy.newProxyInstance(classLoader, new Class[] { Manager.class }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Object result;

                Transaction transaction = method.getAnnotation(Transaction.class);
                if (transaction != null) {
                    SqlSession sqlSession = sqlSessionThreadLocal.get();
                    if (sqlSession == null) {
                        sqlSession = sqlSessionFactory.openSession();
                        sqlSessionThreadLocal.set(sqlSession);
                    }

                    try {
                        result = method.invoke(manager, args);
                        sqlSession.commit();
                    } catch (Exception e) {
                        sqlSession.rollback();
                        throw e;
                    } finally {
                        sqlSessionThreadLocal.remove();
                        sqlSession.close();
                    }
                } else {
                    result = method.invoke(manager, args);
                }

                return result;
            }
        });

        return (Manager) proxy;
    }

}
