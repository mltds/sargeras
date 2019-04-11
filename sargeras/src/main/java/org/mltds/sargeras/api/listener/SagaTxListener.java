package org.mltds.sargeras.api.listener;

import org.mltds.sargeras.api.SagaTxControl;
import org.mltds.sargeras.api.model.SagaTxRecord;

/**
 * @author sunyi.
 */
public interface SagaTxListener {

    void event(SagaTxEvent event);

}
