package com.tronzap.sdk.exception;

/**
 * The API answered with a 2xx status, but the SDK could not interpret the response: the body is not
 * valid JSON, the {@code result} field is missing, or the result does not have the expected shape.
 */
public class InvalidResponseException extends TronzapException {

    private static final long serialVersionUID = 1L;

    /** The HTTP status code. */
    private final int statusCode;
    /** The raw response body. */
    private final String responseBody;

    /**
     * Creates an invalid response exception.
     *
     * @param message what was wrong with the response
     * @param statusCode the HTTP status code
     * @param responseBody the raw response body
     * @param cause the underlying decoding failure, may be {@code null}
     */
    public InvalidResponseException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
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
