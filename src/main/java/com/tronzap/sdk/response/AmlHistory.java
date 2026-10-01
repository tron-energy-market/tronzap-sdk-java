package com.tronzap.sdk.response;

import java.util.List;

/**
 * One page of past AML checks.
 *
 * @param page the 1-based number of this page
 * @param perPage the page size
 * @param total the number of checks matching the query across all pages
 * @param items the checks on this page
 */
public record AmlHistory(int page, int perPage, int total, List<AmlCheck> items) {

    /**
     * Copies the list.
     *
     * @param page the 1-based number of this page
     * @param perPage the page size
     * @param total the number of checks matching the query across all pages
     * @param items the checks on this page
     */
    public AmlHistory {
        items = List.copyOf(items);
    }
}
