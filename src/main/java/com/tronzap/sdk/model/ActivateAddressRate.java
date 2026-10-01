package com.tronzap.sdk.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The price of an address activation.
 *
 * @param price the flat activation fee
 */
public record ActivateAddressRate(BigDecimal price) {

    /**
     * Validates the components.
     *
     * @param price the flat activation fee
     */
    public ActivateAddressRate {
        Objects.requireNonNull(price, "price");
    }
}
