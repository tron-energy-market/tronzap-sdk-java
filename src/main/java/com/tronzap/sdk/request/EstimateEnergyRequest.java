package com.tronzap.sdk.request;

import java.util.Optional;

/**
 * Asks how much energy a token transfer between two addresses needs.
 *
 * @param fromAddress the sender's TRON address
 * @param toAddress the recipient's TRON address
 * @param contractAddress the TRC20 contract to estimate against; when empty the API uses {@link
 *     #USDT_CONTRACT_ADDRESS}
 */
public record EstimateEnergyRequest(String fromAddress, String toAddress, Optional<String> contractAddress) {

    /** The USDT (TRC20) contract the API estimates against when no contract address is given. */
    public static final String USDT_CONTRACT_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

    /**
     * Validates the components.
     *
     * @param fromAddress the sender's TRON address
     * @param toAddress the recipient's TRON address
     * @param contractAddress the TRC20 contract to estimate against
     * @throws IllegalArgumentException if an address is missing or blank
     */
    public EstimateEnergyRequest {
        Checks.required(fromAddress, "fromAddress");
        Checks.required(toAddress, "toAddress");
        Checks.optional(contractAddress, "contractAddress");
    }

    /**
     * Creates a request that estimates a USDT (TRC20) transfer.
     *
     * @param fromAddress the sender's TRON address
     * @param toAddress the recipient's TRON address
     * @return the request
     * @throws IllegalArgumentException if an address is missing or blank
     */
    public static EstimateEnergyRequest of(String fromAddress, String toAddress) {
        return new EstimateEnergyRequest(fromAddress, toAddress, Optional.empty());
    }

    /**
     * Creates a request that estimates a transfer of the given TRC20 token.
     *
     * @param fromAddress the sender's TRON address
     * @param toAddress the recipient's TRON address
     * @param contractAddress the TRC20 contract address
     * @return the request
     * @throws IllegalArgumentException if an address is missing or blank
     */
    public static EstimateEnergyRequest of(String fromAddress, String toAddress, String contractAddress) {
        return new EstimateEnergyRequest(fromAddress, toAddress, Optional.of(Checks.required(contractAddress, "contractAddress")));
    }
}
