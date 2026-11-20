package com.tronzap.sdk.exception;

import com.tronzap.sdk.model.BulkRejectedRow;
import java.time.Duration;
import java.util.List;

/**
 * No row of a bulk order is valid: code {@link ApiErrorCode#BULK_NO_VALID_ITEMS}. {@link
 * #getRejected()} says why each row was rejected.
 */
public class BulkNoValidItemsException extends ApiException {

    private static final long serialVersionUID = 1L;

    /** The rejected rows; not kept when the exception is serialized. */
    private final transient List<BulkRejectedRow> rejected;

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
     * @param rejected the rejected rows
     */
    public BulkNoValidItemsException(
            String message,
            String errorKey,
            String requestId,
            int statusCode,
            String responseBody,
            String result,
            Duration retryAfter,
            List<BulkRejectedRow> rejected) {
        super(message, ApiErrorCode.BULK_NO_VALID_ITEMS.code(), errorKey, requestId, statusCode, responseBody, result, retryAfter);
        this.rejected = List.copyOf(rejected);
    }

    /**
     * Returns the rejected rows with their row numbers and reasons.
     *
     * @return the rejected rows, or {@code null} after deserialization
     */
    public List<BulkRejectedRow> getRejected() {
        return rejected;
    }
}
