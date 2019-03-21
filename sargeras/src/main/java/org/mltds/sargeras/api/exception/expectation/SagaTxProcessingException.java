package org.mltds.sargeras.api.exception.expectation;

import org.mltds.sargeras.api.exception.SagaException;

/**
 * @author sunyi.
 */
public class SagaTxProcessingException extends SagaException implements Processing {

    public SagaTxProcessingException() {
    }

    public SagaTxProcessingException(String message) {
        super(message);
    }

    public SagaTxProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SagaTxProcessingException(Throwable cause) {
        super(cause);
    }

    public SagaTxProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
