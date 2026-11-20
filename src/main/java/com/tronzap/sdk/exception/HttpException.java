package com.tronzap.sdk.exception;

import java.time.Duration;
import java.util.Optional;

/**
 * The API answered with a non-2xx HTTP status and no usable API payload.
 *
 * <p>The narrower subclasses {@link RateLimitException}, {@link UnauthorizedException} and {@link
 * ServerException} cover the statuses worth handling separately.
 */
public class HttpException extends TronzapException {

    private static final long serialVersionUID = 1L;

    /** The HTTP status code. */
    private final int statusCode;
    /** The raw response body. */
    private final String responseBody;
    /** How long to wait before retrying, or {@code null}. */
    private final Duration retryAfter;

    /**
     * Creates an HTTP exception.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     */
    public HttpException(String message, int statusCode, String responseBody) {
        this(message, statusCode, responseBody, null);
    }

    /**
     * Creates an HTTP exception with the wait the server asked for.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     * @param retryAfter how long to wait before retrying, may be {@code null}
     */
    public HttpException(String message, int statusCode, String responseBody, Duration retryAfter) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody == null ? "" : responseBody;
        this.retryAfter = retryAfter;
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the raw response body.
     *
     * @return the response body, never {@code null}
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Returns how long to wait before retrying, from the {@code Retry-After} header (seconds or an
     * HTTP date) or the {@code retry_after} field.
     *
     * @return the wait, or empty when the server sent none
     */
    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
