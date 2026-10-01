package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Looks up one transaction by the identifier the API assigned, by your own external identifier, or
 * by both. At least one of them is required.
 *
 * @param id the identifier the API assigned to the transaction
 * @param externalId the identifier you supplied when creating the transaction
 */
public record CheckTransactionRequest(Optional<String> id, Optional<String> externalId) {

    /**
     * Validates the components.
     *
     * @param id the identifier the API assigned to the transaction
     * @param externalId the identifier you supplied when creating the transaction
     * @throws IllegalArgumentException if both identifiers are empty or one of them is blank
     */
    public CheckTransactionRequest {
        Checks.optional(id, "id");
        Checks.optional(externalId, "externalId");
        if (id.isEmpty() && externalId.isEmpty()) {
            throw new IllegalArgumentException("either id or externalId is required");
        }
    }

    /**
     * Creates a request that looks the transaction up by the identifier the API assigned.
     *
     * @param id the transaction ID
     * @return the request
     * @throws IllegalArgumentException if the ID is missing or blank
     */
    public static CheckTransactionRequest byId(String id) {
        return new CheckTransactionRequest(Optional.of(Checks.required(id, "id")), Optional.empty());
    }

    /**
     * Creates a request that looks the transaction up by your own identifier.
     *
     * @param externalId the external ID supplied when the transaction was created
     * @return the request
     * @throws IllegalArgumentException if the external ID is missing or blank
     */
    public static CheckTransactionRequest byExternalId(String externalId) {
        return new CheckTransactionRequest(Optional.empty(), Optional.of(Checks.required(externalId, "externalId")));
    }
}
