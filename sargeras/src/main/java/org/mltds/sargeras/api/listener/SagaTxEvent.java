package org.mltds.sargeras.api.listener;

import org.apache.commons.beanutils.BeanUtils;
import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.api.model.SagaTxRecord;

/**
 * @author sunyi.
 */
public class SagaTxEvent {

    private final SagaTxEventType eventType;
    private final SagaTxRecord record;

    public SagaTxEvent(SagaTxEventType eventType, SagaTxRecord record) {
        this.eventType = eventType;

        this.record = new SagaTxRecord();
        try {
            BeanUtils.copyProperties(this.record, record);
        } catch (Exception e) {
            throw new SagaException(e);
        }

    }

    public SagaTxEventType getEventType() {
        return eventType;
    }

    public SagaTxRecord getRecord() {
        return record;
    }

}