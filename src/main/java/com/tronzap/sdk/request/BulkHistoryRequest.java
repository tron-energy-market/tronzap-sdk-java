package com.tronzap.sdk.request;

/**
 * Pages through your bulk orders, newest first.
 *
 * @param page the 1-based page number
 * @param perPage the page size; the API accepts at most 50
 */
public record BulkHistoryRequest(int page, int perPage) {

    /** The page size used when none is given. */
    public static final int DEFAULT_PER_PAGE = 10;

    /**
     * Validates the components.
     *
     * @param page the 1-based page number
     * @param perPage the page size; the API accepts at most 50
     * @throws IllegalArgumentException if a number is not positive
     */
    public BulkHistoryRequest {
        Checks.positive(page, "page");
        Checks.positive(perPage, "perPage");
    }

    /**
     * Creates a request for the first page with the default page size.
     *
     * @return the request
     */
    public static BulkHistoryRequest firstPage() {
        return new BulkHistoryRequest(1, DEFAULT_PER_PAGE);
    }

    /**
     * Creates a request for one page.
     *
     * @param page the 1-based page number
     * @param perPage the page size; the API accepts at most 50
     * @return the request
     * @throws IllegalArgumentException if a number is not positive
     */
    public static BulkHistoryRequest of(int page, int perPage) {
        return new BulkHistoryRequest(page, perPage);
    }
}
