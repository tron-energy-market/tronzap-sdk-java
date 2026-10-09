package com.tronzap.sdk.request;

import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlType;
import java.util.Objects;
import java.util.Optional;

/**
 * Starts an AML screening of an address or of a transaction hash.
 *
 * <p>For a {@link AmlType#HASH} check, {@code address} is the recipient address of the transaction,
 * where the funds were received, and {@code direction} says which side of the transaction you are
 * on: {@link AmlDirection#DEPOSIT} if the funds were sent to your address ({@code address} is your
 * address), {@link AmlDirection#WITHDRAWAL} if you sent them ({@code address} is the external
 * recipient's address). The risk is scored for the counterparty: the sender of a deposit, the
 * recipient of a withdrawal.
 *
 * @param type what to screen
 * @param network the blockchain network code, for example {@code TRX}, {@code BTC} or {@code ETH}
 * @param address the address to screen; for a {@link AmlType#HASH} check, the recipient address of
 *     the transaction
 * @param hash the transaction hash, required for a {@link AmlType#HASH} check
 * @param direction which side of the transaction you are on, for a {@link AmlType#HASH} check; when
 *     empty, the SDK sends {@link AmlDirection#DEPOSIT}
 */
public record AmlCheckRequest(
        AmlType type, String network, String address, Optional<String> hash, Optional<AmlDirection> direction) {

    /**
     * Validates the components.
     *
     * @param type what to screen
     * @param network the blockchain network code
     * @param address the address to screen; for a {@link AmlType#HASH} check, the recipient address
     *     of the transaction
     * @param hash the transaction hash, required for a {@link AmlType#HASH} check
     * @param direction which side of the transaction you are on, for a {@link AmlType#HASH} check;
     *     when empty, the SDK sends {@link AmlDirection#DEPOSIT}
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
     * Creates a request that screens an incoming transaction: the funds were sent to your address, so
     * {@code address} is your address and the sender is scored. The SDK sends {@link
     * AmlDirection#DEPOSIT}.
     *
     * @param network the blockchain network code, for example {@code BTC}
     * @param address your address, where the funds were received
     * @param hash the transaction hash
     * @return the request
     * @throws IllegalArgumentException if a value is missing or blank
     */
    public static AmlCheckRequest forHash(String network, String address, String hash) {
        return new AmlCheckRequest(AmlType.HASH, network, address, Optional.ofNullable(hash), Optional.empty());
    }

    /**
     * Creates a request that screens the transaction {@code hash} on {@code network}.
     *
     * <p>{@code address} is the recipient address of the transaction, where the funds were received.
     * {@code direction} says which side of the transaction you are on: {@link AmlDirection#DEPOSIT}
     * if the funds were sent to your address ({@code address} is your address), {@link
     * AmlDirection#WITHDRAWAL} if you sent them ({@code address} is the external recipient's
     * address). The risk is scored for the counterparty: the sender of a deposit, the recipient of a
     * withdrawal.
     *
     * @param network the blockchain network code, for example {@code BTC}
     * @param address the recipient address of the transaction
     * @param hash the transaction hash
     * @param direction which side of the transaction you are on; {@code null} sends {@link
     *     AmlDirection#DEPOSIT}
     * @return the request
     * @throws IllegalArgumentException if a value is missing, blank or {@link AmlDirection#UNKNOWN}
     */
    public static AmlCheckRequest forHash(String network, String address, String hash, AmlDirection direction) {
        return new AmlCheckRequest(AmlType.HASH, network, address, Optional.ofNullable(hash), Optional.ofNullable(direction));
    }
}
