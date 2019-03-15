package org.mltds.sargeras.worker.spi;

/**
 * @author sunyi
 */
public interface SagaBeanFactory<T> {

    T getObject();

}
