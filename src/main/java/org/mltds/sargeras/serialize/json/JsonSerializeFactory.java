package org.mltds.sargeras.serialize.json;

import org.mltds.sargeras.serialize.Serialize;
import org.mltds.sargeras.serialize.SerializeFactory;

/**
 * @author sunyi 2019/2/20.
 */
public class JsonSerializeFactory implements SerializeFactory {

    private JsonSerialize jsonSerialize;

    @Override
    public Serialize getObject() {
        if (jsonSerialize == null) {
            synchronized (JsonSerializeFactory.class) {
                if (jsonSerialize == null) {
                    this.jsonSerialize = new JsonSerialize();
                }
            }
        }
        return this.jsonSerialize;
    }

}
