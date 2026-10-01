package com.tronzap.sdk.exception;

/**
 * The connection to the API could not be established, for example because the host name did not resolve or the connection was refused.
 */
public class ConnectionException extends NetworkException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the detail message
     * @param cause the underlying transport failure, may be {@code null}
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
