package com.tronzap.sdk.exception;

/**
 * The request never produced an HTTP response.
 *
 * <p>The narrower subclasses {@link ConnectionException}, {@link RequestTimeoutException}, {@link
 * SslException} and {@link RequestInterruptedException} classify the failure; this class itself is
 * thrown for any other transport error. The original exception is available as {@link #getCause()}.
 */
public class NetworkException extends TronzapException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a network exception.
     *
     * @param message the detail message
     * @param cause the underlying transport failure, may be {@code null}
     */
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
