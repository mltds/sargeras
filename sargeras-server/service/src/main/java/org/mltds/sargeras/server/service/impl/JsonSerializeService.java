package org.mltds.sargeras.server.service.impl;

import org.mltds.sargeras.server.service.SerializeService;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;

/**
 * @author sunyi
 */
@Service
public class JsonSerializeService implements SerializeService {
    @Override
    public <T> T read(String data, Class<T> c) {
        return JSON.parseObject(data, c);
    }

    @Override
    public String write(Object object) {
        return JSON.toJSONString(object);
    }
}
