package com.tronzap.sdk.request;

import java.util.Objects;

/**
 * One row of a bulk energy order. The SDK does not check the address or the amount: the calculation
 * rejects invalid rows and reports them with their row number.
 *
 * @param address the TRON address that receives the energy
 * @param amount the amount of energy to buy
 */
public record BulkItem(String address, long amount) {

    /**
     * Validates the components.
     *
     * @param address the TRON address that receives the energy
     * @param amount the amount of energy to buy
     * @throws NullPointerException if the address is {@code null}
     */
    public BulkItem {
        Objects.requireNonNull(address, "address");
    }

    /**
     * Creates a row.
     *
     * @param address the TRON address that receives the energy
     * @param amount the amount of energy to buy
     * @return the row
     */
    public static BulkItem of(String address, long amount) {
        return new BulkItem(address, amount);
    }
}
