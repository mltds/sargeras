package org.mltds.sargeras.serialize.json;

import org.mltds.sargeras.serialize.Serialize;
import org.mltds.sargeras.serialize.SerializeBeanFactory;

/**
 * @author sunyi
 */
public class JsonSerializeBeanFactory implements SerializeBeanFactory {

    private JsonSerialize jsonSerialize;

    @Override
    public Serialize getObject() {
        if (jsonSerialize == null) {
            synchronized (JsonSerializeBeanFactory.class) {
                if (jsonSerialize == null) {
                    this.jsonSerialize = new JsonSerialize();
                }
            }
        }
        return this.jsonSerialize;
    }

}
