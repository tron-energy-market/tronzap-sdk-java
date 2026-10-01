package com.tronzap.sdk.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One direct recharge price tier.
 *
 * @param duration the rental duration in hours this tier applies to
 * @param minEnergy the smallest purchasable energy amount in this tier
 * @param maxEnergy the largest purchasable energy amount in this tier
 * @param price the cost of a single unit of energy
 * @param price32k the price of 32,000 energy at this tier
 * @param price65k the price of 65,000 energy at this tier
 * @param price131k the price of 131,000 energy at this tier
 */
public record DirectRechargeRate(
        int duration,
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
     * @param minEnergy the smallest purchasable energy amount in this tier
     * @param maxEnergy the largest purchasable energy amount in this tier
     * @param price the cost of a single unit of energy
     * @param price32k the price of 32,000 energy at this tier
     * @param price65k the price of 65,000 energy at this tier
     * @param price131k the price of 131,000 energy at this tier
     */
    public DirectRechargeRate {
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(price32k, "price32k");
        Objects.requireNonNull(price65k, "price65k");
        Objects.requireNonNull(price131k, "price131k");
    }
}
