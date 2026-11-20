package com.tronzap.sdk.response;

import java.util.List;

/**
 * One page of your bulk orders.
 *
 * @param page the 1-based number of this page
 * @param perPage the page size
 * @param total the number of orders across all pages
 * @param items the orders on this page
 */
public record BulkHistory(int page, int perPage, int total, List<BulkOrder> items) {

    /**
     * Copies the list.
     *
     * @param page the 1-based number of this page
     * @param perPage the page size
     * @param total the number of orders across all pages
     * @param items the orders on this page
     */
    public BulkHistory {
        items = List.copyOf(items);
    }
}
