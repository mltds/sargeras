package org.mltds.sargeras.api.exception.expectation;

import org.mltds.sargeras.api.exception.SagaException;

/**
 * @author sunyi.
 */
public class SagaTxFailureException extends SagaException implements Failure {

    public SagaTxFailureException() {
    }

    public SagaTxFailureException(String message) {
        super(message);
    }

    public SagaTxFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public SagaTxFailureException(Throwable cause) {
        super(cause);
    }

    public SagaTxFailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
