package com.tronzap.sdk.response;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The account balance.
 *
 * @param balance the available account balance
 * @param address the TRON address your account deposits to
 */
public record Balance(BigDecimal balance, String address) {

    /**
     * Validates the components.
     *
     * @param balance the available account balance
     * @param address the TRON address your account deposits to
     */
    public Balance {
        Objects.requireNonNull(balance, "balance");
        Objects.requireNonNull(address, "address");
    }
}
