package com.tronzap.sdk.response;

import com.tronzap.sdk.model.Service;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * The price of a resource purchase.
 *
 * @param address the address the quote was made for
 * @param type the resource type that was priced
 * @param amount the resource amount that was priced
 * @param energy the same value as {@link #amount()}, deprecated
 * @param duration the rental duration in hours
 * @param price the cost of the resources
 * @param activationFee the address activation fee included in {@code total}, zero when none
 * @param total the total cost
 */
public record Calculation(
        String address,
        Service type,
        long amount,
        long energy,
        int duration,
        BigDecimal price,
        BigDecimal activationFee,
        BigDecimal total) {

    /**
     * Validates the components.
     *
     * @param address the address the quote was made for
     * @param type the resource type that was priced
     * @param amount the resource amount that was priced
     * @param energy the same value as {@code amount}
     * @param duration the rental duration in hours
     * @param price the cost of the resources
     * @param activationFee the address activation fee included in {@code total}, zero when none
     * @param total the total cost
     */
    public Calculation {
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(activationFee, "activationFee");
        Objects.requireNonNull(total, "total");
    }

    /**
     * Returns the priced energy amount.
     *
     * @return the same value as {@link #amount()}
     * @deprecated use {@link #amount()}
     */
    @Deprecated
    @Override
    public long energy() {
        return energy;
    }
}
