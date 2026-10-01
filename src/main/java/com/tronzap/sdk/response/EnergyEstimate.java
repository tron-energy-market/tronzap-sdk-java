package com.tronzap.sdk.response;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The energy a transfer needs and what it would cost.
 *
 * @param amount the estimated resource amount
 * @param energy the estimated energy amount
 * @param duration the rental duration in hours the price refers to
 * @param price the cost of the energy
 * @param activationFee the address activation fee included in {@code total}, zero when none
 * @param total the total cost
 * @param fromAddress the sender address
 * @param toAddress the recipient address
 * @param contractAddress the contract the estimate was made against
 */
public record EnergyEstimate(
        long amount,
        long energy,
        int duration,
        BigDecimal price,
        BigDecimal activationFee,
        BigDecimal total,
        String fromAddress,
        String toAddress,
        String contractAddress) {

    /**
     * Validates the components.
     *
     * @param amount the estimated resource amount
     * @param energy the estimated energy amount
     * @param duration the rental duration in hours the price refers to
     * @param price the cost of the energy
     * @param activationFee the address activation fee included in {@code total}, zero when none
     * @param total the total cost
     * @param fromAddress the sender address
     * @param toAddress the recipient address
     * @param contractAddress the contract the estimate was made against
     */
    public EnergyEstimate {
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(activationFee, "activationFee");
        Objects.requireNonNull(total, "total");
        Objects.requireNonNull(fromAddress, "fromAddress");
        Objects.requireNonNull(toAddress, "toAddress");
        Objects.requireNonNull(contractAddress, "contractAddress");
    }
}
