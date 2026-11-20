package com.tronzap.sdk.exception;

import java.time.Duration;

/**
 * HTTP status 5xx: the API failed to process the request.
 */
public class ServerException extends HttpException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     */
    public ServerException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }

    /**
     * Creates the exception with the wait the server asked for.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     * @param retryAfter how long to wait before retrying, may be {@code null}
     */
    public ServerException(String message, int statusCode, String responseBody, Duration retryAfter) {
        super(message, statusCode, responseBody, retryAfter);
    }
}
