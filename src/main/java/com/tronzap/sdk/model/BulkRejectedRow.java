package com.tronzap.sdk.model;

import java.util.Objects;

/**
 * A row of a bulk order that the calculation did not accept.
 *
 * @param row the row's position in the request items, starting at 1
 * @param address the address as sent
 * @param reason why the row was rejected, such as {@code invalid_address} or {@code duplicate}
 */
public record BulkRejectedRow(int row, String address, String reason) {

    /**
     * Validates the components.
     *
     * @param row the row's position in the request items, starting at 1
     * @param address the address as sent
     * @param reason why the row was rejected, such as {@code invalid_address} or {@code duplicate}
     */
    public BulkRejectedRow {
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(reason, "reason");
    }
}
