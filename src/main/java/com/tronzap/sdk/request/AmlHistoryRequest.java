package com.tronzap.sdk.request;

import com.tronzap.sdk.model.AmlStatus;
import java.util.Objects;
import java.util.Optional;

/**
 * Pages through past AML checks, newest first.
 *
 * @param page the 1-based page number
 * @param perPage the page size
 * @param status only return checks with this status
 */
public record AmlHistoryRequest(int page, int perPage, Optional<AmlStatus> status) {

    /** The page size used when none is given. */
    public static final int DEFAULT_PER_PAGE = 10;

    /**
     * Validates the components.
     *
     * @param page the 1-based page number
     * @param perPage the page size
     * @param status only return checks with this status
     * @throws IllegalArgumentException if a number is not positive or the status is {@link
     *     AmlStatus#UNKNOWN}
     */
    public AmlHistoryRequest {
        Checks.positive(page, "page");
        Checks.positive(perPage, "perPage");
        Objects.requireNonNull(status, "status");
        status.ifPresent(value -> Checks.known(value, AmlStatus.UNKNOWN, "status"));
    }

    /**
     * Creates a request for the first page with the default page size and no status filter.
     *
     * @return the request
     */
    public static AmlHistoryRequest firstPage() {
        return new AmlHistoryRequest(1, DEFAULT_PER_PAGE, Optional.empty());
    }

    /**
     * Creates a request for one page without a status filter.
     *
     * @param page the 1-based page number
     * @param perPage the page size
     * @return the request
     * @throws IllegalArgumentException if a number is not positive
     */
    public static AmlHistoryRequest of(int page, int perPage) {
        return new AmlHistoryRequest(page, perPage, Optional.empty());
    }

    /**
     * Creates a request for one page of checks with the given status.
     *
     * @param page the 1-based page number
     * @param perPage the page size
     * @param status only return checks with this status
     * @return the request
     * @throws IllegalArgumentException if a number is not positive or the status is invalid
     */
    public static AmlHistoryRequest of(int page, int perPage, AmlStatus status) {
        return new AmlHistoryRequest(page, perPage, Optional.of(Checks.known(status, AmlStatus.UNKNOWN, "status")));
    }
}
