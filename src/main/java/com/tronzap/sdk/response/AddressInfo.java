package com.tronzap.sdk.response;

import com.tronzap.sdk.model.Resources;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * The on-chain resources and token balances of an address.
 *
 * @param resources the currently available energy and bandwidth
 * @param balances token symbols such as {@code TRX} or {@code USDT} mapped to their balances
 */
public record AddressInfo(Resources resources, Map<String, BigDecimal> balances) {

    /**
     * Validates the components and copies the map.
     *
     * @param resources the currently available energy and bandwidth
     * @param balances token symbols such as {@code TRX} or {@code USDT} mapped to their balances
     */
    public AddressInfo {
        Objects.requireNonNull(resources, "resources");
        balances = Map.copyOf(balances);
    }
}
