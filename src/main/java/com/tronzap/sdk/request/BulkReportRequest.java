package com.tronzap.sdk.request;

import com.tronzap.sdk.model.BulkRowStatus;
import java.util.Objects;
import java.util.Optional;

/**
 * Selects the report of one bulk order by the identifier the API assigned, by your own external
 * identifier, or by both. At least one of them is required.
 *
 * @param id the identifier the API assigned to the order
 * @param externalId the identifier you supplied when starting the order
 * @param status only report the rows with this result
 */
public record BulkReportRequest(Optional<String> id, Optional<String> externalId, Optional<BulkRowStatus> status) {

    /**
     * Validates the components.
     *
     * @param id the identifier the API assigned to the order
     * @param externalId the identifier you supplied when starting the order
     * @param status only report the rows with this result
     * @throws IllegalArgumentException if both identifiers are empty, one of them is blank or the
     *     status is {@link BulkRowStatus#UNKNOWN}
     */
    public BulkReportRequest {
        Checks.optional(id, "id");
        Checks.optional(externalId, "externalId");
        if (id.isEmpty() && externalId.isEmpty()) {
            throw new IllegalArgumentException("either id or externalId is required");
        }
        Objects.requireNonNull(status, "status");
        status.ifPresent(value -> Checks.known(value, BulkRowStatus.UNKNOWN, "status"));
    }

    /**
     * Creates a request for the whole report of the order with the identifier the API assigned.
     *
     * @param id the bulk order ID
     * @return the request
     * @throws IllegalArgumentException if the ID is missing or blank
     */
    public static BulkReportRequest byId(String id) {
        return new BulkReportRequest(Optional.of(Checks.required(id, "id")), Optional.empty(), Optional.empty());
    }

    /**
     * Creates a request for the whole report of the order with your own identifier.
     *
     * @param externalId the external ID supplied when the order was started
     * @return the request
     * @throws IllegalArgumentException if the external ID is missing or blank
     */
    public static BulkReportRequest byExternalId(String externalId) {
        return new BulkReportRequest(
                Optional.empty(), Optional.of(Checks.required(externalId, "externalId")), Optional.empty());
    }

    /**
     * Returns a copy of this request that only reports the rows with the given result.
     *
     * @param status the result to keep
     * @return the request
     * @throws IllegalArgumentException if the status is missing or {@link BulkRowStatus#UNKNOWN}
     */
    public BulkReportRequest withStatus(BulkRowStatus status) {
        return new BulkReportRequest(id, externalId, Optional.of(Checks.known(status, BulkRowStatus.UNKNOWN, "status")));
    }
}
