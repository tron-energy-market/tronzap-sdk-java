package com.tronzap.sdk.exception;

import java.util.Optional;

/**
 * The API answered with a well-formed payload whose {@code code} is not {@code 0}.
 *
 * <p>It is thrown regardless of the HTTP status, because the API reports some failures with a 2xx
 * status and others with a 4xx or 5xx status. Branch on {@link #getErrorCode()}:
 *
 * <pre>{@code
 * try {
 *     client.createEnergyTransaction(request);
 * } catch (ApiException e) {
 *     if (e.getErrorCode() == ApiErrorCode.INSUFFICIENT_FUNDS) {
 *         // top up the balance
 *     }
 * }
 * }</pre>
 */
public class ApiException extends TronzapException {

    private static final long serialVersionUID = 1L;

    /** The API error code. */
    private final int code;
    /** The machine-readable error alias, or {@code null}. */
    private final String errorKey;
    /** The API request identifier, or {@code null}. */
    private final String requestId;
    /** The HTTP status code. */
    private final int statusCode;
    /** The raw response body. */
    private final String responseBody;

    /**
     * Creates an API exception.
     *
     * @param message the human-readable message from the {@code error} field
     * @param code the API error code
     * @param errorKey the machine-readable alias from the {@code key} field, may be {@code null}
     * @param requestId the API request identifier, may be {@code null}
     * @param statusCode the HTTP status the payload arrived with
     * @param responseBody the raw response body
     */
    public ApiException(
            String message,
            int code,
            String errorKey,
            String requestId,
            int statusCode,
            String responseBody) {
        super(message);
        this.code = code;
        this.errorKey = errorKey;
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.responseBody = responseBody == null ? "" : responseBody;
    }

    /**
     * Returns the numeric API error code as sent by the API.
     *
     * @return the API error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the API error code as a constant.
     *
     * @return the matching constant, or {@link ApiErrorCode#UNKNOWN} for a code this SDK does not know
     */
    public ApiErrorCode getErrorCode() {
        return ApiErrorCode.fromCode(code);
    }

    /**
     * Returns the machine-readable error alias, such as {@code invalid_tron_address} or the sub-key
     * {@code invalid_tron_address.from_address}.
     *
     * @return the error key, or empty when the API sent none
     */
    public Optional<String> getErrorKey() {
        return Optional.ofNullable(errorKey);
    }

    /**
     * Returns the identifier of the request on the API side. Quote it in support requests.
     *
     * @return the request identifier, or empty when the API sent none
     */
    public Optional<String> getRequestId() {
        return Optional.ofNullable(requestId);
    }

    /**
     * Returns the HTTP status code the error payload arrived with.
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
