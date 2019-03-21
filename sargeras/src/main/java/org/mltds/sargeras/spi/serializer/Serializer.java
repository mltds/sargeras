package org.mltds.sargeras.spi.serializer;

import org.mltds.sargeras.spi.SagaBean;

/**
 * @author sunyi.
 */
public interface Serializer extends SagaBean {

    byte[] serialize(Object object);

    <T> T deserialize(byte[] bytes, Class<T> cls);

}
