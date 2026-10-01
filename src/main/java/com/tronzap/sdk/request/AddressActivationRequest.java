package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Activates a TRON address. An address must be activated once before it can hold resources.
 *
 * @param address the TRON address to activate
 * @param externalId your own identifier for the transaction
 */
public record AddressActivationRequest(String address, Optional<String> externalId) {

    /**
     * Validates the components.
     *
     * @param address the TRON address to activate
     * @param externalId your own identifier for the transaction
     * @throws IllegalArgumentException if the address is missing or the external ID is blank
     */
    public AddressActivationRequest {
        Checks.required(address, "address");
        Checks.optional(externalId, "externalId");
    }

    /**
     * Creates a request without an external ID.
     *
     * @param address the TRON address to activate
     * @return the request
     * @throws IllegalArgumentException if the address is missing
     */
    public static AddressActivationRequest of(String address) {
        return new AddressActivationRequest(address, Optional.empty());
    }

    /**
     * Creates a request with an external ID.
     *
     * @param address the TRON address to activate
     * @param externalId your own identifier for the transaction
     * @return the request
     * @throws IllegalArgumentException if a value is missing or blank
     */
    public static AddressActivationRequest of(String address, String externalId) {
        return new AddressActivationRequest(address, Optional.of(Checks.required(externalId, "externalId")));
    }
}
