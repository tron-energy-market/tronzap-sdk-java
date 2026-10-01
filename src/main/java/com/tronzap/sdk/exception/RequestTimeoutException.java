package com.tronzap.sdk.exception;

/**
 * The request did not complete within the configured timeout. This includes a connect timeout and a response body that arrives too slowly.
 */
public class RequestTimeoutException extends NetworkException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the detail message
     * @param cause the underlying transport failure, may be {@code null}
     */
    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
