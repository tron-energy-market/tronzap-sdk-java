package com.tronzap.sdk.request;

import java.util.List;

/**
 * Validates and prices the rows of a bulk energy order.
 *
 * @param items the rows, at most 10 000
 * @param duration the rental duration in hours, one of the durations the services list offers
 */
public record BulkCalculateRequest(List<BulkItem> items, int duration) {

    /**
     * Validates the components and copies the list.
     *
     * @param items the rows, at most 10 000
     * @param duration the rental duration in hours
     * @throws IllegalArgumentException if there are no rows or the duration is not positive
     * @throws NullPointerException if the list or one of its rows is {@code null}
     */
    public BulkCalculateRequest {
        items = List.copyOf(items);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items is required");
        }
        Checks.positive(duration, "duration");
    }

    /**
     * Creates a request for a one-hour rental.
     *
     * @param items the rows, at most 10 000
     * @return the request
     * @throws IllegalArgumentException if there are no rows
     */
    public static BulkCalculateRequest of(List<BulkItem> items) {
        return new BulkCalculateRequest(items, 1);
    }
}
