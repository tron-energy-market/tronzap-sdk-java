package com.tronzap.sdk.response;

import com.tronzap.sdk.model.DirectRechargeRate;
import java.util.List;
import java.util.Objects;

/**
 * The direct recharge service: pay the returned address directly and energy is delivered at the
 * listed rates.
 *
 * @param address the TronZap address to send payment to
 * @param rates the available energy rates
 */
public record DirectRechargeInfo(String address, List<DirectRechargeRate> rates) {

    /**
     * Validates the components and copies the list.
     *
     * @param address the TronZap address to send payment to
     * @param rates the available energy rates
     */
    public DirectRechargeInfo {
        Objects.requireNonNull(address, "address");
        rates = List.copyOf(rates);
    }
}
