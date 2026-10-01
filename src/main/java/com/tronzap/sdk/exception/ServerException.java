package com.tronzap.sdk.exception;

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
}
