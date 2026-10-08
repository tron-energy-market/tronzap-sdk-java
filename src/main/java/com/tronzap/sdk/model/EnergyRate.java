package com.tronzap.sdk.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One energy price tier.
 *
 * <p>{@link #price()} is the cost of 1000 units of energy, so 65000 energy at a price of {@code 0.03}
 * costs {@code 1.95}. Bandwidth is priced the same way, see {@link BandwidthRate#price()}.
 *
 * @param duration the rental duration in hours this tier applies to
 * @param minAmount the smallest purchasable amount in this tier
 * @param maxAmount the largest purchasable amount in this tier
 * @param minEnergy the same value as {@link #minAmount()}, deprecated
 * @param maxEnergy the same value as {@link #maxAmount()}, deprecated
 * @param price the cost of 1000 units of energy
 * @param price32k the price of 32,000 energy at this tier
 * @param price65k the price of 65,000 energy at this tier
 * @param price131k the price of 131,000 energy at this tier
 */
public record EnergyRate(
        int duration,
        long minAmount,
        long maxAmount,
        long minEnergy,
        long maxEnergy,
        BigDecimal price,
        BigDecimal price32k,
        BigDecimal price65k,
        BigDecimal price131k) {

    /**
     * Validates the components.
     *
     * @param duration the rental duration in hours this tier applies to
     * @param minAmount the smallest purchasable amount in this tier
     * @param maxAmount the largest purchasable amount in this tier
     * @param minEnergy the same value as {@code minAmount}
     * @param maxEnergy the same value as {@code maxAmount}
     * @param price the cost of 1000 units of energy
     * @param price32k the price of 32,000 energy at this tier
     * @param price65k the price of 65,000 energy at this tier
     * @param price131k the price of 131,000 energy at this tier
     */
    public EnergyRate {
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(price32k, "price32k");
        Objects.requireNonNull(price65k, "price65k");
        Objects.requireNonNull(price131k, "price131k");
    }

    /**
     * Returns the smallest purchasable amount in this tier.
     *
     * @return the same value as {@link #minAmount()}
     * @deprecated use {@link #minAmount()}
     */
    @Deprecated
    @Override
    public long minEnergy() {
        return minEnergy;
    }

    /**
     * Returns the largest purchasable amount in this tier.
     *
     * @return the same value as {@link #maxAmount()}
     * @deprecated use {@link #maxAmount()}
     */
    @Deprecated
    @Override
    public long maxEnergy() {
        return maxEnergy;
    }
}
