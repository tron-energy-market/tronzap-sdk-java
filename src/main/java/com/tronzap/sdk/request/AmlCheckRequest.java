package com.tronzap.sdk.request;

import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlType;
import java.util.Objects;
import java.util.Optional;

/**
 * Starts an AML screening of an address or of a transaction hash.
 *
 * @param type what to screen
 * @param network the blockchain network code, for example {@code TRX}, {@code BTC} or {@code ETH}
 * @param address the address to screen; for a {@link AmlType#HASH} check, the recipient address of
 *     the transaction
 * @param hash the transaction hash, required for a {@link AmlType#HASH} check
 * @param direction the transaction direction for a {@link AmlType#HASH} check; the API defaults to
 *     {@link AmlDirection#DEPOSIT}
 */
public record AmlCheckRequest(
        AmlType type, String network, String address, Optional<String> hash, Optional<AmlDirection> direction) {

    /**
     * Validates the components.
     *
     * @param type what to screen
     * @param network the blockchain network code
     * @param address the address to screen
     * @param hash the transaction hash, required for a {@link AmlType#HASH} check
     * @param direction the transaction direction for a {@link AmlType#HASH} check
     * @throws IllegalArgumentException if a required value is missing or a value is invalid
     */
    public AmlCheckRequest {
        Checks.known(type, AmlType.UNKNOWN, "type");
        Checks.required(network, "network");
        Checks.required(address, "address");
        Checks.optional(hash, "hash");
        Objects.requireNonNull(direction, "direction");
        direction.ifPresent(value -> Checks.known(value, AmlDirection.UNKNOWN, "direction"));
        if (type == AmlType.HASH && hash.isEmpty()) {
            throw new IllegalArgumentException("hash is required for a hash check");
        }
    }

    /**
     * Creates a request that screens a wallet address.
     *
     * @param network the blockchain network code, for example {@code TRX}
     * @param address the address to screen
     * @return the request
     * @throws IllegalArgumentException if a value is missing or blank
     */
    public static AmlCheckRequest forAddress(String network, String address) {
        return new AmlCheckRequest(AmlType.ADDRESS, network, address, Optional.empty(), Optional.empty());
    }

    /**
     * Creates a request that screens an incoming transaction.
     *
     * @param network the blockchain network code, for example {@code BTC}
     * @param address the recipient address of the transaction
     * @param hash the transaction hash
     * @return the request
     * @throws IllegalArgumentException if a value is missing or blank
     */
    public static AmlCheckRequest forHash(String network, String address, String hash) {
        return new AmlCheckRequest(AmlType.HASH, network, address, Optional.ofNullable(hash), Optional.empty());
    }

    /**
     * Creates a request that screens a transaction in the given direction.
     *
     * @param network the blockchain network code, for example {@code BTC}
     * @param address the recipient address of the transaction
     * @param hash the transaction hash
     * @param direction the transaction direction
     * @return the request
     * @throws IllegalArgumentException if a value is missing, blank or {@link AmlDirection#UNKNOWN}
     */
    public static AmlCheckRequest forHash(String network, String address, String hash, AmlDirection direction) {
        return new AmlCheckRequest(AmlType.HASH, network, address, Optional.ofNullable(hash), Optional.ofNullable(direction));
    }
}
