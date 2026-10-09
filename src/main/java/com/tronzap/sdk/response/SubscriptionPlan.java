package com.tronzap.sdk.response;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A subscription plan on sale.
 *
 * @param subscriptionId the plan identifier, such as {@code unlimited_energy}; pass it to {@link
 *     com.tronzap.sdk.request.StartSubscriptionRequest}
 * @param id the plan's numeric identifier
 * @param name the human-readable plan name
 * @param activationFee the one-time fee charged when a subscription starts
 * @param initialPrice the amount charged when a subscription starts
 * @param price the cost of each transaction the subscription serves
 * @param transactionsLimit how many transactions the plan covers, {@code 0} for no limit
 * @param durationDays how many days the plan runs, {@code 0} for no time limit
 */
public record SubscriptionPlan(
        String subscriptionId,
        long id,
        String name,
        BigDecimal activationFee,
        BigDecimal initialPrice,
        BigDecimal price,
        long transactionsLimit,
        int durationDays) {

    /**
     * Validates the components.
     *
     * @param subscriptionId the plan identifier, such as {@code unlimited_energy}
     * @param id the plan's numeric identifier
     * @param name the human-readable plan name
     * @param activationFee the one-time fee charged when a subscription starts
     * @param initialPrice the amount charged when a subscription starts
     * @param price the cost of each transaction the subscription serves
     * @param transactionsLimit how many transactions the plan covers, {@code 0} for no limit
     * @param durationDays how many days the plan runs, {@code 0} for no time limit
     */
    public SubscriptionPlan {
        Objects.requireNonNull(subscriptionId, "subscriptionId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(activationFee, "activationFee");
        Objects.requireNonNull(initialPrice, "initialPrice");
        Objects.requireNonNull(price, "price");
    }
}
