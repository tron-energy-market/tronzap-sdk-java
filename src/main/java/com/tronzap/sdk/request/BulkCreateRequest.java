package com.tronzap.sdk.request;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Starts a bulk order from a calculation.
 *
 * @param id the identifier of the calculation returned by the bulk calculation
 * @param externalId your own identifier for the order, unique per account
 * @param force whether to buy even if the price rises while the order runs, within {@code
 *     maxPriceIncrease}
 * @param maxPriceIncrease the allowed price increase in percent, 0 to 100, used only with {@code
 *     force}; the API default of 20 applies when it is empty
 * @param webhookUrl a public {@code https} URL called once when the order becomes final
 */
public record BulkCreateRequest(
        String id, Optional<String> externalId, boolean force, OptionalInt maxPriceIncrease, Optional<String> webhookUrl) {

    /**
     * Validates the components.
     *
     * @param id the identifier of the calculation
     * @param externalId your own identifier for the order
     * @param force whether to buy even if the price rises while the order runs
     * @param maxPriceIncrease the allowed price increase in percent
     * @param webhookUrl the URL called once when the order becomes final
     * @throws IllegalArgumentException if the ID is missing or an optional value is blank
     */
    public BulkCreateRequest {
        Checks.required(id, "id");
        Checks.optional(externalId, "externalId");
        Objects.requireNonNull(maxPriceIncrease, "maxPriceIncrease");
        Checks.optional(webhookUrl, "webhookUrl");
    }

    /**
     * Creates a request without options.
     *
     * @param id the identifier of the calculation
     * @return the request
     * @throws IllegalArgumentException if the ID is missing or blank
     */
    public static BulkCreateRequest of(String id) {
        return builder(id).build();
    }

    /**
     * Starts a builder with the required value.
     *
     * @param id the identifier of the calculation
     * @return a new builder
     */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** Builds a {@link BulkCreateRequest}. A builder is not safe for concurrent use. */
    public static final class Builder {

        private final String id;
        private String externalId;
        private boolean force;
        private Integer maxPriceIncrease;
        private String webhookUrl;

        private Builder(String id) {
            this.id = id;
        }

        /**
         * Sets your own identifier for the order. Starting an order again with the same external ID
         * returns the order already started.
         *
         * @param externalId the external ID, or {@code null} for none
         * @return this builder
         */
        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        /**
         * Sets whether to buy even if the price rises while the order runs. The default is {@code false}:
         * an address whose price rose is skipped.
         *
         * @param force whether to allow price increases
         * @return this builder
         */
        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        /**
         * Sets the allowed price increase in percent, 0 to 100, used only with {@link #force(boolean)}.
         *
         * @param maxPriceIncrease the allowed increase in percent
         * @return this builder
         */
        public Builder maxPriceIncrease(int maxPriceIncrease) {
            this.maxPriceIncrease = maxPriceIncrease;
            return this;
        }

        /**
         * Sets a public {@code https} URL called once when the order becomes final. Check its {@code
         * X-Signature} header with {@link com.tronzap.sdk.TronzapClient#verifyWebhookSignature(byte[],
         * String)}.
         *
         * @param webhookUrl the webhook URL, or {@code null} for none
         * @return this builder
         */
        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        /**
         * Builds the request.
         *
         * @return the request
         * @throws IllegalArgumentException if a value is invalid
         */
        public BulkCreateRequest build() {
            return new BulkCreateRequest(
                    id,
                    Optional.ofNullable(externalId),
                    force,
                    maxPriceIncrease == null ? OptionalInt.empty() : OptionalInt.of(maxPriceIncrease),
                    Optional.ofNullable(webhookUrl));
        }
    }
}
