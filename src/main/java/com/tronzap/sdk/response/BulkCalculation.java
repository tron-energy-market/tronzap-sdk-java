package com.tronzap.sdk.response;

import com.tronzap.sdk.model.BulkRejectedRow;
import com.tronzap.sdk.model.BulkStatus;
import com.tronzap.sdk.model.Timestamp;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A priced bulk order that was not started yet. Start it with its {@link #id()} before it expires.
 *
 * @param id the identifier to start the order with
 * @param status always {@link BulkStatus#DRAFT}
 * @param resource the resource ordered, always {@code energy}
 * @param duration the rental duration in hours
 * @param totalAmount the estimated total of the accepted rows, in TRX
 * @param accepted the number of accepted rows
 * @param rejectedCount the number of rejected rows
 * @param rejected the rejected rows
 * @param expiresAt when the calculation expires
 */
public record BulkCalculation(
        String id,
        BulkStatus status,
        String resource,
        int duration,
        BigDecimal totalAmount,
        int accepted,
        int rejectedCount,
        List<BulkRejectedRow> rejected,
        Optional<Timestamp> expiresAt) {

    /**
     * Validates the components and copies the list.
     *
     * @param id the identifier to start the order with
     * @param status always {@link BulkStatus#DRAFT}
     * @param resource the resource ordered, always {@code energy}
     * @param duration the rental duration in hours
     * @param totalAmount the estimated total of the accepted rows, in TRX
     * @param accepted the number of accepted rows
     * @param rejectedCount the number of rejected rows
     * @param rejected the rejected rows
     * @param expiresAt when the calculation expires
     */
    public BulkCalculation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(totalAmount, "totalAmount");
        rejected = List.copyOf(rejected);
        Objects.requireNonNull(expiresAt, "expiresAt");
    }
}
