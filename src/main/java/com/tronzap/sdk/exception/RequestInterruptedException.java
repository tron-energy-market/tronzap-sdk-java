package com.tronzap.sdk.exception;

/**
 * The calling thread was interrupted while waiting for the response. The thread's interrupt status is restored before this exception is thrown.
 */
public class RequestInterruptedException extends NetworkException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the detail message
     * @param cause the underlying transport failure, may be {@code null}
     */
    public RequestInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
