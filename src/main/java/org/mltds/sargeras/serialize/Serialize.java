package org.mltds.sargeras.serialize;

public interface Serialize {
    <T> T read(String data, Class<T> c);

    String write(Object object);

}