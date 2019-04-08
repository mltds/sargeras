package org.mltds.sargeras.api.listener;

/**
 * @author sunyi.
 */
public enum SagaEventType {

    ON_TRIGGER_FIRST,

    ON_TRIGGER_NOT_FIRST,

    ON_EXECUTE_SUCC,

    ON_EXECUTE_FAIL,

    ON_COMPENSATE_SUCC,

    ON_COMPENSATE_FAIL,

    ON_OVERTIME

}
