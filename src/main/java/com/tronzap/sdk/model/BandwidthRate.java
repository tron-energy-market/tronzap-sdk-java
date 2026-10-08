package com.tronzap.sdk.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One bandwidth price tier.
 *
 * <p>{@link #price()} is the cost of 1000 units of bandwidth, so 345 bandwidth at a price of {@code
 * 1} costs {@code 0.345}. Energy is priced the same way, see {@link EnergyRate#price()}.
 *
 * @param duration the rental duration in hours this tier applies to
 * @param minAmount the smallest purchasable amount in this tier
 * @param maxAmount the largest purchasable amount in this tier
 * @param price the cost of 1000 units of bandwidth
 */
public record BandwidthRate(int duration, long minAmount, long maxAmount, BigDecimal price) {

    /**
     * Validates the components.
     *
     * @param duration the rental duration in hours this tier applies to
     * @param minAmount the smallest purchasable amount in this tier
     * @param maxAmount the largest purchasable amount in this tier
     * @param price the cost of 1000 units of bandwidth
     */
    public BandwidthRate {
        Objects.requireNonNull(price, "price");
    }
}
