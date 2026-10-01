package com.tronzap.sdk.request;

/**
 * Prices an energy purchase without creating a transaction.
 *
 * @param address the TRON address that would receive the energy
 * @param energy the amount of energy to price
 * @param duration the rental duration in hours
 */
public record CalculateRequest(String address, long energy, int duration) {

    /**
     * Validates the components.
     *
     * @param address the TRON address that would receive the energy
     * @param energy the amount of energy to price
     * @param duration the rental duration in hours
     * @throws IllegalArgumentException if the address is missing or a number is not positive
     */
    public CalculateRequest {
        Checks.required(address, "address");
        Checks.positive(energy, "energy");
        Checks.positive(duration, "duration");
    }

    /**
     * Creates a request for a one-hour rental.
     *
     * @param address the TRON address that would receive the energy
     * @param energy the amount of energy to price
     * @return the request
     * @throws IllegalArgumentException if the address is missing or the energy is not positive
     */
    public static CalculateRequest of(String address, long energy) {
        return new CalculateRequest(address, energy, 1);
    }
}
