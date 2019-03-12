package org.mltds.sargeras.exception;

/**
 * Saga Exception， 为了方便识别是不是Saga发生的异常
 * 
 * @author sunyi
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
