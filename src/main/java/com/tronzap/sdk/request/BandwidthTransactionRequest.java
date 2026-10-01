package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Buys bandwidth for an address. Bandwidth is always rented for one hour.
 *
 * @param address the TRON address that receives the bandwidth
 * @param bandwidth the amount of bandwidth to buy
 * @param externalId your own identifier for the transaction
 */
public record BandwidthTransactionRequest(String address, long bandwidth, Optional<String> externalId) {

    /**
     * Validates the components.
     *
     * @param address the TRON address that receives the bandwidth
     * @param bandwidth the amount of bandwidth to buy
     * @param externalId your own identifier for the transaction
     * @throws IllegalArgumentException if the address is missing, the bandwidth is not positive or the
     *     external ID is blank
     */
    public BandwidthTransactionRequest {
        Checks.required(address, "address");
        Checks.positive(bandwidth, "bandwidth");
        Checks.optional(externalId, "externalId");
    }

    /**
     * Creates a request without an external ID.
     *
     * @param address the TRON address that receives the bandwidth
     * @param bandwidth the amount of bandwidth to buy
     * @return the request
     * @throws IllegalArgumentException if the address is missing or the bandwidth is not positive
     */
    public static BandwidthTransactionRequest of(String address, long bandwidth) {
        return new BandwidthTransactionRequest(address, bandwidth, Optional.empty());
    }

    /**
     * Creates a request with an external ID.
     *
     * @param address the TRON address that receives the bandwidth
     * @param bandwidth the amount of bandwidth to buy
     * @param externalId your own identifier for the transaction
     * @return the request
     * @throws IllegalArgumentException if a value is missing or invalid
     */
    public static BandwidthTransactionRequest of(String address, long bandwidth, String externalId) {
        return new BandwidthTransactionRequest(address, bandwidth, Optional.of(Checks.required(externalId, "externalId")));
    }
}
