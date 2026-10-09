package com.tronzap.sdk.model;

import java.util.Objects;

/**
 * The parameters a subscription was started with, as echoed by the API.
 *
 * @param address the address the subscription serves
 * @param durationDays how many days the subscription runs, {@code 0} for no time limit
 * @param transactionsLimit how many transactions the subscription covers, {@code 0} for no limit
 * @param activateAddress whether address activation was requested
 */
public record SubscriptionParams(String address, int durationDays, long transactionsLimit, boolean activateAddress) {

    /**
     * Validates the components.
     *
     * @param address the address the subscription serves
     * @param durationDays how many days the subscription runs, {@code 0} for no time limit
     * @param transactionsLimit how many transactions the subscription covers, {@code 0} for no limit
     * @param activateAddress whether address activation was requested
     */
    public SubscriptionParams {
        Objects.requireNonNull(address, "address");
    }
}
