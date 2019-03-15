package org.mltds.sargeras.server.service;

public interface SerializeService {

    <T> T read(String data, Class<T> c);

    String write(Object object);

}