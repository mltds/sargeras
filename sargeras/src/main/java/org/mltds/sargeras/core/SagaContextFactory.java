package org.mltds.sargeras.core;

import org.mltds.sargeras.api.model.SagaRecord;
import org.mltds.sargeras.spi.manager.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author sunyi.
 */
@Component
public class SagaContextFactory {

    @Autowired
    private SagaApplication sagaApplication;

    @Autowired
    private Manager manager;



    public  SagaContext loadContext(String appName, String bizName, String bizId) {
        SagaContext context = new SagaContext();

        SagaRecord record = manager.findRecord(appName, bizName, bizId);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);

        context.manager = manager;
        context.record = record;
        context.saga = sagaApplication.getSaga(appName, bizName);

        return context;
    }

    public  SagaContext loadContext(long recordId) {
        SagaContext context = new SagaContext();

        SagaRecord record = manager.findRecord(recordId);

        String triggerId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        record.setTriggerId(triggerId);

        context.manager = manager;
        context.record = record;
        context.saga = sagaApplication.getSaga(context.getAppName(), context.getBizName());

        return context;
    }
}
