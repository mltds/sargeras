package org.mltds.sargeras.component;

/**
 * @author sunyi 2019/2/22.
 */
public class SagaException extends RuntimeException {

    public SagaException() {
        super();
    }

    public SagaException(String message) {
        super(message);
    }

    public SagaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SagaException(Throwable cause) {
        super(cause);
    }

    protected SagaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
