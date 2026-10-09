package com.tronzap.sdk.response;

import com.tronzap.sdk.model.SubscriptionParams;
import com.tronzap.sdk.model.SubscriptionStatus;
import com.tronzap.sdk.model.Timestamp;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * An energy subscription for an address.
 *
 * <p>Starting, checking and stopping a subscription report its identifiers, status, dates and
 * {@link #params()}. The subscription history reports the usage counters {@link #transactionsUsed()},
 * {@link #energyUsed()} and {@link #totalPrice()} instead of the params. Counters a response does not
 * carry are zero, and other values it does not carry are empty.
 *
 * @param id the identifier assigned by the API
 * @param subscriptionId the plan the subscription belongs to, such as {@code unlimited_energy}
 * @param externalId the identifier you supplied, empty if you supplied none
 * @param address the address the subscription serves, empty when the response does not carry it
 * @param status the current status
 * @param params the parameters the subscription was started with, empty in the history
 * @param transactionsLimit how many transactions the subscription covers, {@code 0} for no limit
 * @param transactionsUsed how many transactions the subscription has served
 * @param energyUsed how much energy the subscription has delegated
 * @param totalPrice the amount charged for the subscription so far
 * @param createdAt when the subscription was created
 * @param startedAt when the subscription started
 * @param renewedAt when the subscription was last renewed, empty if never
 * @param stoppedAt when the subscription was stopped, empty if it was not
 * @param expireAt when the subscription ends, empty if it has no time limit
 */
public record Subscription(
        String id,
        String subscriptionId,
        Optional<String> externalId,
        Optional<String> address,
        SubscriptionStatus status,
        Optional<SubscriptionParams> params,
        long transactionsLimit,
        long transactionsUsed,
        long energyUsed,
        BigDecimal totalPrice,
        Optional<Timestamp> createdAt,
        Optional<Timestamp> startedAt,
        Optional<Timestamp> renewedAt,
        Optional<Timestamp> stoppedAt,
        Optional<Timestamp> expireAt) {

    /**
     * Validates the components.
     *
     * @param id the identifier assigned by the API
     * @param subscriptionId the plan the subscription belongs to, such as {@code unlimited_energy}
     * @param externalId the identifier you supplied, empty if you supplied none
     * @param address the address the subscription serves, empty when the response does not carry it
     * @param status the current status
     * @param params the parameters the subscription was started with, empty in the history
     * @param transactionsLimit how many transactions the subscription covers, {@code 0} for no limit
     * @param transactionsUsed how many transactions the subscription has served
     * @param energyUsed how much energy the subscription has delegated
     * @param totalPrice the amount charged for the subscription so far
     * @param createdAt when the subscription was created
     * @param startedAt when the subscription started
     * @param renewedAt when the subscription was last renewed, empty if never
     * @param stoppedAt when the subscription was stopped, empty if it was not
     * @param expireAt when the subscription ends, empty if it has no time limit
     */
    public Subscription {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(subscriptionId, "subscriptionId");
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(totalPrice, "totalPrice");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(renewedAt, "renewedAt");
        Objects.requireNonNull(stoppedAt, "stoppedAt");
        Objects.requireNonNull(expireAt, "expireAt");
    }
}
