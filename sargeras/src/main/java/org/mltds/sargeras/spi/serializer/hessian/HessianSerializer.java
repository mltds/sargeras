package org.mltds.sargeras.spi.serializer.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.spi.serializer.Serializer;
import org.springframework.stereotype.Component;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;

/**
 * @author sunyi.
 */
@Component
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {

        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(os);
            output.writeObject(object);
            byte[] bytes = os.toByteArray();
            return bytes;
        } catch (IOException e) {
            throw new SagaException("序列化数据时失败, Object Cls: " + object.getClass().getName());
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> cls) {
        ByteArrayInputStream is = null;
        Hessian2Input his = null;
        try {
            is = new ByteArrayInputStream(bytes);
            his = new Hessian2Input(is);
            his.close();
            return (T) his.readObject(cls);
        } catch (IOException e) {
            throw new SagaException("反序列化数据时失败, Cls: " + cls.getName());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
