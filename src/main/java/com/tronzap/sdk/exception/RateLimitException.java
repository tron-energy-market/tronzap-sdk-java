package com.tronzap.sdk.exception;

/**
 * HTTP status 429 Too Many Requests: slow down before retrying.
 */
public class RateLimitException extends HttpException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     */
    public RateLimitException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
}
