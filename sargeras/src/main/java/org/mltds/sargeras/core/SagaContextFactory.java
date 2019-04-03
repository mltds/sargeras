package org.mltds.sargeras.core;

import java.util.UUID;

import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.model.SagaRecord;
import org.mltds.sargeras.spi.manager.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author sunyi.
 */
@Component
public class SagaContextFactory {

    @Autowired
    private SagaApplication sagaApplication;

    @Autowired
    private Manager manager;

    public SagaContext newContext(Saga saga) {
        SagaContext context = new SagaContext();
        context.saga = saga;
        context.manager = manager;
        return context;
    }

    public SagaContext loadContext(String appName, String bizName, String bizId) {

        SagaRecord record = manager.findRecord(appName, bizName, bizId);
        if (record == null) {
            return null;
        }
        SagaContext context = new SagaContext();

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);

        context.manager = manager;
        context.record = record;
        context.saga = sagaApplication.getSaga(appName, bizName);

        return context;
    }

    public SagaContext loadContext(long recordId) {

        SagaRecord record = manager.findRecord(recordId);
        if (record == null) {
            return null;
        }

        SagaContext context = new SagaContext();

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);

        context.manager = manager;
        context.record = record;
        context.saga = sagaApplication.getSaga(context.getAppName(), context.getBizName());

        return context;
    }
}
