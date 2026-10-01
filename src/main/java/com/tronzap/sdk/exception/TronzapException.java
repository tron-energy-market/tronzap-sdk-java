package com.tronzap.sdk.exception;

/**
 * Base class of every exception thrown by a TronZap API call.
 *
 * <p>It is unchecked, so callers catch it only where they can handle the failure. Catch a subclass
 * to react to one kind of failure.
 */
public class TronzapException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message the detail message
     */
    public TronzapException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message the detail message
     * @param cause the underlying failure, may be {@code null}
     */
    public TronzapException(String message, Throwable cause) {
        super(message, cause);
    }
}
