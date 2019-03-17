package org.mltds.sargeras.spi.manager.rdbms;

import com.alibaba.fastjson.JSON;

/**
 * @author sunyi
 */
public class JsonSerialize {
    public <T> T read(String data, Class<T> c) {
        return JSON.parseObject(data, c);
    }

    public String write(Object object) {
        return JSON.toJSONString(object);
    }
}
