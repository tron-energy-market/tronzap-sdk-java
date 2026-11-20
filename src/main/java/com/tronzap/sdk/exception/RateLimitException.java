package com.tronzap.sdk.exception;

import java.time.Duration;

/**
 * HTTP status 429 Too Many Requests without an API payload, such as one from a proxy: slow down
 * before retrying.
 *
 * <p>A rate limit reported by the API itself arrives as an {@link ApiException} with code {@link
 * ApiErrorCode#RATE_LIMITED}. Both carry the wait in {@code getRetryAfter()}.
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

    /**
     * Creates the exception with the wait the server asked for.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     * @param retryAfter how long to wait before retrying, may be {@code null}
     */
    public RateLimitException(String message, int statusCode, String responseBody, Duration retryAfter) {
        super(message, statusCode, responseBody, retryAfter);
    }
}
