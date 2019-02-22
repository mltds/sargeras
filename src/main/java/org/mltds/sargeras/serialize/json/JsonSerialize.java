package org.mltds.sargeras.serialize.json;

import com.alibaba.fastjson.JSON;
import org.mltds.sargeras.serialize.Serialize;

/**
 * @author sunyi 2019/2/20.
 */
public class JsonSerialize implements Serialize {
    @Override
    public <T> T read(String data, Class<T> c) {
        return JSON.parseObject(data, c);
    }

    @Override
    public String write(Object object) {
        return JSON.toJSONString(object);
    }
}
