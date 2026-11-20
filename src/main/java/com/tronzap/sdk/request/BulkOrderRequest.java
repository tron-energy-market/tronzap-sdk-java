package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Looks up one bulk order by the identifier the API assigned, by your own external identifier, or by
 * both. At least one of them is required.
 *
 * @param id the identifier the API assigned to the order
 * @param externalId the identifier you supplied when starting the order
 */
public record BulkOrderRequest(Optional<String> id, Optional<String> externalId) {

    /**
     * Validates the components.
     *
     * @param id the identifier the API assigned to the order
     * @param externalId the identifier you supplied when starting the order
     * @throws IllegalArgumentException if both identifiers are empty or one of them is blank
     */
    public BulkOrderRequest {
        Checks.optional(id, "id");
        Checks.optional(externalId, "externalId");
        if (id.isEmpty() && externalId.isEmpty()) {
            throw new IllegalArgumentException("either id or externalId is required");
        }
    }

    /**
     * Creates a request that looks the order up by the identifier the API assigned.
     *
     * @param id the bulk order ID
     * @return the request
     * @throws IllegalArgumentException if the ID is missing or blank
     */
    public static BulkOrderRequest byId(String id) {
        return new BulkOrderRequest(Optional.of(Checks.required(id, "id")), Optional.empty());
    }

    /**
     * Creates a request that looks the order up by your own identifier.
     *
     * @param externalId the external ID supplied when the order was started
     * @return the request
     * @throws IllegalArgumentException if the external ID is missing or blank
     */
    public static BulkOrderRequest byExternalId(String externalId) {
        return new BulkOrderRequest(Optional.empty(), Optional.of(Checks.required(externalId, "externalId")));
    }
}
