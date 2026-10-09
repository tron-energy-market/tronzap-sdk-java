package com.tronzap.sdk.request;

import com.tronzap.sdk.model.SubscriptionStatus;
import java.util.Objects;
import java.util.Optional;

/**
 * Pages through your subscriptions, newest first.
 *
 * @param page the 1-based page number
 * @param perPage the page size; the API accepts at most 50
 * @param status only return subscriptions with this status
 */
public record SubscriptionHistoryRequest(int page, int perPage, Optional<SubscriptionStatus> status) {

    /** The page size used when none is given. */
    public static final int DEFAULT_PER_PAGE = 10;

    /**
     * Validates the components.
     *
     * @param page the 1-based page number
     * @param perPage the page size; the API accepts at most 50
     * @param status only return subscriptions with this status
     * @throws IllegalArgumentException if a number is not positive or the status is {@link
     *     SubscriptionStatus#UNKNOWN}
     */
    public SubscriptionHistoryRequest {
        Checks.positive(page, "page");
        Checks.positive(perPage, "perPage");
        Objects.requireNonNull(status, "status");
        status.ifPresent(value -> Checks.known(value, SubscriptionStatus.UNKNOWN, "status"));
    }

    /**
     * Creates a request for the first page with the default page size and no status filter.
     *
     * @return the request
     */
    public static SubscriptionHistoryRequest firstPage() {
        return new SubscriptionHistoryRequest(1, DEFAULT_PER_PAGE, Optional.empty());
    }

    /**
     * Creates a request for one page without a status filter.
     *
     * @param page the 1-based page number
     * @param perPage the page size; the API accepts at most 50
     * @return the request
     * @throws IllegalArgumentException if a number is not positive
     */
    public static SubscriptionHistoryRequest of(int page, int perPage) {
        return new SubscriptionHistoryRequest(page, perPage, Optional.empty());
    }

    /**
     * Creates a request for one page of subscriptions with the given status.
     *
     * @param page the 1-based page number
     * @param perPage the page size; the API accepts at most 50
     * @param status only return subscriptions with this status
     * @return the request
     * @throws IllegalArgumentException if a number is not positive or the status is invalid
     */
    public static SubscriptionHistoryRequest of(int page, int perPage, SubscriptionStatus status) {
        return new SubscriptionHistoryRequest(
                page, perPage, Optional.of(Checks.known(status, SubscriptionStatus.UNKNOWN, "status")));
    }
}
