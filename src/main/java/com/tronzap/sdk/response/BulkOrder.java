package com.tronzap.sdk.response;

import com.tronzap.sdk.model.BulkStatus;
import com.tronzap.sdk.model.Timestamp;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A started bulk order and its progress.
 *
 * <p>Every accepted address ends up delivered, failed, in review or skipped; until then it is
 * pending, and {@link #processed()} counts the addresses with a final result.
 *
 * @param id the identifier assigned by the API
 * @param externalId the identifier you supplied, empty if you supplied none
 * @param status the current status
 * @param resource the resource ordered, always {@code energy}
 * @param duration the rental duration in hours
 * @param force whether price increases are allowed
 * @param maxPriceIncrease the allowed price increase in percent, {@code 0} without {@code force}
 * @param webhookUrl the URL called when the order becomes final, empty if none
 * @param total the number of accepted addresses
 * @param processed the number of addresses with a final result
 * @param delivered the number of addresses energy was delivered to
 * @param failed the number of addresses not delivered and refunded
 * @param inReview the number of addresses support is checking
 * @param skipped the number of addresses not bought and not charged
 * @param skippedByReason the skipped addresses counted by reason, such as {@code
 *     address_not_activated} or {@code price_increased}
 * @param rejectedCount the number of rows the calculation rejected
 * @param totalAmount the estimated total from the calculation, in TRX
 * @param chargedAmount the amount charged so far, in TRX; it may decrease while addresses in review
 *     are refunded
 * @param createdAt when the calculation was created
 * @param startedAt when the order started
 * @param finalizedAt when the order became final, empty while it is processing
 */
public record BulkOrder(
        String id,
        Optional<String> externalId,
        BulkStatus status,
        String resource,
        int duration,
        boolean force,
        int maxPriceIncrease,
        Optional<String> webhookUrl,
        int total,
        int processed,
        int delivered,
        int failed,
        int inReview,
        int skipped,
        Map<String, Integer> skippedByReason,
        int rejectedCount,
        BigDecimal totalAmount,
        BigDecimal chargedAmount,
        Optional<Timestamp> createdAt,
        Optional<Timestamp> startedAt,
        Optional<Timestamp> finalizedAt) {

    /**
     * Validates the components and copies the map.
     *
     * @param id the identifier assigned by the API
     * @param externalId the identifier you supplied, empty if you supplied none
     * @param status the current status
     * @param resource the resource ordered, always {@code energy}
     * @param duration the rental duration in hours
     * @param force whether price increases are allowed
     * @param maxPriceIncrease the allowed price increase in percent, {@code 0} without {@code force}
     * @param webhookUrl the URL called when the order becomes final, empty if none
     * @param total the number of accepted addresses
     * @param processed the number of addresses with a final result
     * @param delivered the number of addresses energy was delivered to
     * @param failed the number of addresses not delivered and refunded
     * @param inReview the number of addresses support is checking
     * @param skipped the number of addresses not bought and not charged
     * @param skippedByReason the skipped addresses counted by reason
     * @param rejectedCount the number of rows the calculation rejected
     * @param totalAmount the estimated total from the calculation, in TRX
     * @param chargedAmount the amount charged so far, in TRX
     * @param createdAt when the calculation was created
     * @param startedAt when the order started
     * @param finalizedAt when the order became final, empty while it is processing
     */
    public BulkOrder {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(webhookUrl, "webhookUrl");
        skippedByReason = Map.copyOf(skippedByReason);
        Objects.requireNonNull(totalAmount, "totalAmount");
        Objects.requireNonNull(chargedAmount, "chargedAmount");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(finalizedAt, "finalizedAt");
    }
}
