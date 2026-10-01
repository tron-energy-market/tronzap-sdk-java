package com.tronzap.sdk.exception;

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

    /**
     * Creates an HTTP exception.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     */
    public HttpException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody == null ? "" : responseBody;
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
}
