package org.mltds.sargeras.spi.serializer;

/**
 * @author sunyi.
 */
public interface Serializer {

    byte[] serialize(Object object);

    <T> T deserialize(byte[] bytes, Class<T> cls);

}
