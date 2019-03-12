package org.mltds.sargeras.serialize.json;

import org.mltds.sargeras.serialize.Serialize;

import com.alibaba.fastjson.JSON;

/**
 * @author sunyi
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
