package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Looks up one subscription by the identifier the API assigned, by your own external identifier, or
 * by both. At least one of them is required.
 *
 * @param id the identifier the API assigned to the subscription
 * @param externalId the identifier you supplied when starting the subscription
 */
public record SubscriptionRequest(Optional<String> id, Optional<String> externalId) {

    /**
     * Validates the components.
     *
     * @param id the identifier the API assigned to the subscription
     * @param externalId the identifier you supplied when starting the subscription
     * @throws IllegalArgumentException if both identifiers are empty or one of them is blank
     */
    public SubscriptionRequest {
        Checks.optional(id, "id");
        Checks.optional(externalId, "externalId");
        if (id.isEmpty() && externalId.isEmpty()) {
            throw new IllegalArgumentException("either id or externalId is required");
        }
    }

    /**
     * Creates a request that looks the subscription up by the identifier the API assigned.
     *
     * @param id the subscription ID
     * @return the request
     * @throws IllegalArgumentException if the ID is missing or blank
     */
    public static SubscriptionRequest byId(String id) {
        return new SubscriptionRequest(Optional.of(Checks.required(id, "id")), Optional.empty());
    }

    /**
     * Creates a request that looks the subscription up by your own identifier.
     *
     * @param externalId the external ID supplied when the subscription was started
     * @return the request
     * @throws IllegalArgumentException if the external ID is missing or blank
     */
    public static SubscriptionRequest byExternalId(String externalId) {
        return new SubscriptionRequest(Optional.empty(), Optional.of(Checks.required(externalId, "externalId")));
    }
}
