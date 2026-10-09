package com.tronzap.sdk.response;

import java.util.List;

/**
 * One page of your subscriptions.
 *
 * @param page the 1-based number of this page
 * @param perPage the page size
 * @param total the number of subscriptions matching the query across all pages
 * @param items the subscriptions on this page
 */
public record SubscriptionHistory(int page, int perPage, int total, List<Subscription> items) {

    /**
     * Copies the list.
     *
     * @param page the 1-based number of this page
     * @param perPage the page size
     * @param total the number of subscriptions matching the query across all pages
     * @param items the subscriptions on this page
     */
    public SubscriptionHistory {
        items = List.copyOf(items);
    }
}
