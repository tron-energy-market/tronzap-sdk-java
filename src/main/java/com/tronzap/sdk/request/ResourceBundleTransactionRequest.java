package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Buys energy and bandwidth for an address in one transaction.
 *
 * @param address the TRON address that receives the resources
 * @param energy the amount of energy to buy
 * @param bandwidth the amount of bandwidth to buy
 * @param duration the rental duration in hours
 * @param externalId your own identifier for the transaction
 * @param activateAddress whether to also activate the address if it is not active yet
 */
public record ResourceBundleTransactionRequest(
        String address,
        long energy,
        long bandwidth,
        int duration,
        Optional<String> externalId,
        boolean activateAddress) {

    /**
     * Validates the components.
     *
     * @param address the TRON address that receives the resources
     * @param energy the amount of energy to buy
     * @param bandwidth the amount of bandwidth to buy
     * @param duration the rental duration in hours
     * @param externalId your own identifier for the transaction
     * @param activateAddress whether to also activate the address if it is not active yet
     * @throws IllegalArgumentException if the address is missing, a number is not positive or the
     *     external ID is blank
     */
    public ResourceBundleTransactionRequest {
        Checks.required(address, "address");
        Checks.positive(energy, "energy");
        Checks.positive(bandwidth, "bandwidth");
        Checks.positive(duration, "duration");
        Checks.optional(externalId, "externalId");
    }

    /**
     * Creates a request for a one-hour rental without an external ID.
     *
     * @param address the TRON address that receives the resources
     * @param energy the amount of energy to buy
     * @param bandwidth the amount of bandwidth to buy
     * @return the request
     * @throws IllegalArgumentException if the address is missing or an amount is not positive
     */
    public static ResourceBundleTransactionRequest of(String address, long energy, long bandwidth) {
        return builder(address, energy, bandwidth).build();
    }

    /**
     * Starts a builder with the required values.
     *
     * @param address the TRON address that receives the resources
     * @param energy the amount of energy to buy
     * @param bandwidth the amount of bandwidth to buy
     * @return a new builder
     */
    public static Builder builder(String address, long energy, long bandwidth) {
        return new Builder(address, energy, bandwidth);
    }

    /** Builds a {@link ResourceBundleTransactionRequest}. A builder is not safe for concurrent use. */
    public static final class Builder {

        private final String address;
        private final long energy;
        private final long bandwidth;
        private int duration = 1;
        private String externalId;
        private boolean activateAddress;

        private Builder(String address, long energy, long bandwidth) {
            this.address = address;
            this.energy = energy;
            this.bandwidth = bandwidth;
        }

        /**
         * Sets the rental duration in hours. The default is {@code 1}.
         *
         * @param duration the rental duration in hours
         * @return this builder
         */
        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Sets your own identifier for the transaction.
         *
         * @param externalId the external ID, or {@code null} for none
         * @return this builder
         */
        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        /**
         * Sets whether to also activate the address if it is not active yet. The default is {@code false}.
         *
         * @param activateAddress whether to activate the address
         * @return this builder
         */
        public Builder activateAddress(boolean activateAddress) {
            this.activateAddress = activateAddress;
            return this;
        }

        /**
         * Builds the request.
         *
         * @return the request
         * @throws IllegalArgumentException if a value is invalid
         */
        public ResourceBundleTransactionRequest build() {
            return new ResourceBundleTransactionRequest(
                    address, energy, bandwidth, duration, Optional.ofNullable(externalId), activateAddress);
        }
    }
}
