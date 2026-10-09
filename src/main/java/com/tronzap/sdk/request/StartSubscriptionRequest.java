package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Starts a subscription for an address.
 *
 * @param subscriptionId the plan to subscribe to: the {@link
 *     com.tronzap.sdk.response.SubscriptionPlan#subscriptionId()} of a plan, such as {@code
 *     unlimited_energy}, not its numeric ID
 * @param address the TRON address the subscription serves
 * @param durationDays how many days the subscription runs, {@code 0} for no time limit
 * @param transactionsLimit how many transactions the subscription covers, {@code 0} for no limit
 * @param externalId your own identifier for the subscription
 * @param activateAddress whether to also activate the address if it is not active yet
 */
public record StartSubscriptionRequest(
        String subscriptionId,
        String address,
        int durationDays,
        long transactionsLimit,
        Optional<String> externalId,
        boolean activateAddress) {

    /**
     * Validates the components.
     *
     * @param subscriptionId the plan to subscribe to
     * @param address the TRON address the subscription serves
     * @param durationDays how many days the subscription runs, {@code 0} for no time limit
     * @param transactionsLimit how many transactions the subscription covers, {@code 0} for no limit
     * @param externalId your own identifier for the subscription
     * @param activateAddress whether to also activate the address if it is not active yet
     * @throws IllegalArgumentException if the plan or the address is missing, a number is negative or
     *     the external ID is blank
     */
    public StartSubscriptionRequest {
        Checks.required(subscriptionId, "subscriptionId");
        Checks.required(address, "address");
        Checks.nonNegative(durationDays, "durationDays");
        Checks.nonNegative(transactionsLimit, "transactionsLimit");
        Checks.optional(externalId, "externalId");
    }

    /**
     * Creates a request without a time or transactions limit and without an external ID.
     *
     * @param subscriptionId the plan to subscribe to, such as {@code unlimited_energy}
     * @param address the TRON address the subscription serves
     * @return the request
     * @throws IllegalArgumentException if the plan or the address is missing
     */
    public static StartSubscriptionRequest of(String subscriptionId, String address) {
        return builder(subscriptionId, address).build();
    }

    /**
     * Starts a builder with the required values.
     *
     * @param subscriptionId the plan to subscribe to, such as {@code unlimited_energy}
     * @param address the TRON address the subscription serves
     * @return a new builder
     */
    public static Builder builder(String subscriptionId, String address) {
        return new Builder(subscriptionId, address);
    }

    /** Builds a {@link StartSubscriptionRequest}. A builder is not safe for concurrent use. */
    public static final class Builder {

        private final String subscriptionId;
        private final String address;
        private int durationDays;
        private long transactionsLimit;
        private String externalId;
        private boolean activateAddress;

        private Builder(String subscriptionId, String address) {
            this.subscriptionId = subscriptionId;
            this.address = address;
        }

        /**
         * Sets how many days the subscription runs. The default is {@code 0}, no time limit.
         *
         * @param durationDays the duration in days, {@code 0} for no time limit
         * @return this builder
         */
        public Builder durationDays(int durationDays) {
            this.durationDays = durationDays;
            return this;
        }

        /**
         * Sets how many transactions the subscription covers. The default is {@code 0}, no limit.
         *
         * @param transactionsLimit the transactions limit, {@code 0} for no limit
         * @return this builder
         */
        public Builder transactionsLimit(long transactionsLimit) {
            this.transactionsLimit = transactionsLimit;
            return this;
        }

        /**
         * Sets your own identifier for the subscription.
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
        public StartSubscriptionRequest build() {
            return new StartSubscriptionRequest(
                    subscriptionId, address, durationDays, transactionsLimit, Optional.ofNullable(externalId), activateAddress);
        }
    }
}
