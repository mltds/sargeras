package org.mltds.sargeras.spi.serializer;

/**
 * @author sunyi.
 */
public interface Serializer {

    byte[] encode(Object object);

    <T> T decode(byte[] bytes, Class<T> cls);

}
