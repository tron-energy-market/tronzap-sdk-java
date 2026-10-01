package com.tronzap.sdk.exception;

/**
 * The TLS handshake failed, for example because the server certificate could not be verified.
 */
public class SslException extends NetworkException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the detail message
     * @param cause the underlying transport failure, may be {@code null}
     */
    public SslException(String message, Throwable cause) {
        super(message, cause);
    }
}
