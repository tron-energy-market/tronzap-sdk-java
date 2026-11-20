package com.tronzap.sdk.exception;

import com.tronzap.sdk.response.BulkCalculation;
import java.time.Duration;
import java.util.Objects;

/**
 * The price changed since the bulk calculation, so the order was not started: code {@link
 * ApiErrorCode#BULK_PRICE_CHANGED}. Confirm the new total of {@link #getCalculation()}, then start the
 * order with its ID.
 */
public class BulkPriceChangedException extends ApiException {

    private static final long serialVersionUID = 1L;

    /** The new calculation; not kept when the exception is serialized. */
    private final transient BulkCalculation calculation;

    /**
     * Creates the exception.
     *
     * @param message the human-readable message from the {@code error} field
     * @param errorKey the machine-readable alias from the {@code key} field, may be {@code null}
     * @param requestId the API request identifier, may be {@code null}
     * @param statusCode the HTTP status the payload arrived with
     * @param responseBody the raw response body
     * @param result the {@code result} field as JSON
     * @param retryAfter how long to wait before retrying, may be {@code null}
     * @param calculation the new calculation
     */
    public BulkPriceChangedException(
            String message,
            String errorKey,
            String requestId,
            int statusCode,
            String responseBody,
            String result,
            Duration retryAfter,
            BulkCalculation calculation) {
        super(message, ApiErrorCode.BULK_PRICE_CHANGED.code(), errorKey, requestId, statusCode, responseBody, result, retryAfter);
        this.calculation = Objects.requireNonNull(calculation, "calculation");
    }

    /**
     * Returns the new calculation at the current prices.
     *
     * @return the new calculation, or {@code null} after deserialization
     */
    public BulkCalculation getCalculation() {
        return calculation;
    }
}
