package org.mltds.sargeras.api.listener;

import org.mltds.sargeras.api.model.SagaRecord;

/**
 * Listener 用于监听各个事件或动作。发生的异常都会被
 * 
 * @author sunyi
 */
public interface SagaListener {

    void event(SagaEvent event);

}