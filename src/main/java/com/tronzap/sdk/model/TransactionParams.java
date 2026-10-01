package com.tronzap.sdk.model;

import java.util.Objects;

/**
 * The parameters a transaction was created with, as echoed by the API.
 *
 * <p>The API has reported the purchased amount under several field names over time. They are all
 * folded into {@link #amounts()}.
 *
 * @param address the address that receives the resources
 * @param duration the rental duration in hours, {@code 0} when the API did not report one
 * @param amounts the purchased amounts
 * @param activateAddress whether address activation was requested
 */
public record TransactionParams(String address, int duration, Amounts amounts, boolean activateAddress) {

    /**
     * Validates the components.
     *
     * @param address the address that receives the resources
     * @param duration the rental duration in hours, {@code 0} when the API did not report one
     * @param amounts the purchased amounts
     * @param activateAddress whether address activation was requested
     */
    public TransactionParams {
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(amounts, "amounts");
    }
}
