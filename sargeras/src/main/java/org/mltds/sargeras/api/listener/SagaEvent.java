package org.mltds.sargeras.api.listener;

import org.apache.commons.beanutils.BeanUtils;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.model.SagaRecord;

/**
 * @author sunyi.
 */
public class SagaEvent {

    private final SagaEventType eventType;
    private final SagaRecord record;

    public SagaEvent(SagaEventType eventType, SagaRecord record) {
        this.eventType = eventType;
        this.record = new SagaRecord();
        try {
            BeanUtils.copyProperties(this.record, record);
        } catch (Exception e) {
            throw new SagaException(e);
        }

    }

    public SagaEventType getEventType() {
        return eventType;
    }

    public SagaRecord getRecord() {
        return record;
    }
}
