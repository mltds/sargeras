package org.mltds.sargeras.api;

/**
 * @author sunyi
 */
public enum SagaStatus {

    INIT,

    EXECUTING,

    EXECUTE_SUCC,

    COMPENSATING,

    COMPENSATE_SUCC,

    COMPENSATE_FAIL

}
