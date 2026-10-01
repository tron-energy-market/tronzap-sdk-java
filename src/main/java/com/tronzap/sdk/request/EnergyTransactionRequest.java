package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Buys energy for an address.
 *
 * @param address the TRON address that receives the energy
 * @param energy the amount of energy to buy
 * @param duration the rental duration in hours
 * @param externalId your own identifier for the transaction
 * @param activateAddress whether to also activate the address if it is not active yet
 */
public record EnergyTransactionRequest(
        String address, long energy, int duration, Optional<String> externalId, boolean activateAddress) {

    /**
     * Validates the components.
     *
     * @param address the TRON address that receives the energy
     * @param energy the amount of energy to buy
     * @param duration the rental duration in hours
     * @param externalId your own identifier for the transaction
     * @param activateAddress whether to also activate the address if it is not active yet
     * @throws IllegalArgumentException if the address is missing, a number is not positive or the
     *     external ID is blank
     */
    public EnergyTransactionRequest {
        Checks.required(address, "address");
        Checks.positive(energy, "energy");
        Checks.positive(duration, "duration");
        Checks.optional(externalId, "externalId");
    }

    /**
     * Creates a request for a one-hour rental without an external ID.
     *
     * @param address the TRON address that receives the energy
     * @param energy the amount of energy to buy
     * @return the request
     * @throws IllegalArgumentException if the address is missing or the energy is not positive
     */
    public static EnergyTransactionRequest of(String address, long energy) {
        return builder(address, energy).build();
    }

    /**
     * Starts a builder with the required values.
     *
     * @param address the TRON address that receives the energy
     * @param energy the amount of energy to buy
     * @return a new builder
     */
    public static Builder builder(String address, long energy) {
        return new Builder(address, energy);
    }

    /** Builds an {@link EnergyTransactionRequest}. A builder is not safe for concurrent use. */
    public static final class Builder {

        private final String address;
        private final long energy;
        private int duration = 1;
        private String externalId;
        private boolean activateAddress;

        private Builder(String address, long energy) {
            this.address = address;
            this.energy = energy;
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
        public EnergyTransactionRequest build() {
            return new EnergyTransactionRequest(address, energy, duration, Optional.ofNullable(externalId), activateAddress);
        }
    }
}
