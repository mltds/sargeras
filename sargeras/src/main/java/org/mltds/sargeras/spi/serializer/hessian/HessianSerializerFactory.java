package org.mltds.sargeras.spi.serializer.hessian;

import com.alibaba.com.caucho.hessian.io.SerializerFactory;

public class HessianSerializerFactory extends SerializerFactory {

    public static final SerializerFactory SERIALIZER_FACTORY = new HessianSerializerFactory();

    private HessianSerializerFactory() {
        this.setAllowNonSerializable(true);
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
