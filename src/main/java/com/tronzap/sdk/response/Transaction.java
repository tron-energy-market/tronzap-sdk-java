package com.tronzap.sdk.response;

import com.tronzap.sdk.model.Service;
import com.tronzap.sdk.model.Timestamp;
import com.tronzap.sdk.model.TransactionParams;
import com.tronzap.sdk.model.TransactionStatus;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * A resource purchase or an address activation.
 *
 * <p>The API currently reports a resource bundle with the service {@link Service#ENERGY}, not {@link
 * Service#RESOURCE_BUNDLE}. Read {@link TransactionParams#amounts()} from {@link #params()} to see
 * which resources a transaction contains.
 *
 * @param id the identifier assigned by the API
 * @param externalId the identifier you supplied, empty if you supplied none
 * @param service the purchased service as reported by the API; a resource bundle is reported as
 *     {@link Service#ENERGY}
 * @param params the parameters the transaction was created with
 * @param status the current status
 * @param amount the amount charged to your balance
 * @param createdAt when the transaction was created, empty when the API did not report it
 * @param hash the on-chain transaction hash, empty until the transaction settles
 */
public record Transaction(
        String id,
        Optional<String> externalId,
        Service service,
        TransactionParams params,
        TransactionStatus status,
        BigDecimal amount,
        Optional<Timestamp> createdAt,
        Optional<String> hash) {

    /**
     * Validates the components.
     *
     * @param id the identifier assigned by the API
     * @param externalId the identifier you supplied, empty if you supplied none
     * @param service the purchased service as reported by the API; a resource bundle is reported as
     *     {@link Service#ENERGY}
     * @param params the parameters the transaction was created with
     * @param status the current status
     * @param amount the amount charged to your balance
     * @param createdAt when the transaction was created, empty when the API did not report it
     * @param hash the on-chain transaction hash, empty until the transaction settles
     */
    public Transaction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(hash, "hash");
    }
}
