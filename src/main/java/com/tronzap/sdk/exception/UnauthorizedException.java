package com.tronzap.sdk.exception;

/**
 * HTTP status 401 or 403: the request was not authorized.
 */
public class UnauthorizedException extends HttpException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     */
    public UnauthorizedException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
}
