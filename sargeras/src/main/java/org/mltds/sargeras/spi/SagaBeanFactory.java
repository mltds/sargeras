package org.mltds.sargeras.spi;

/**
 * @author sunyi
 */
public interface SagaBeanFactory<T extends SagaBean> {

    T getObject();

}
