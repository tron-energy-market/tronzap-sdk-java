package com.tronzap.sdk.model;

/**
 * The status of a bulk order. A calculation is a draft; a started order moves from processing to
 * one of the final statuses: completed, partial or failed.
 */
public enum BulkStatus {

    /** A calculation that was not started. */
    DRAFT("draft"),

    /** The addresses are being processed. */
    PROCESSING("processing"),

    /** Energy was delivered to every address. */
    COMPLETED("completed"),

    /** Energy was delivered to some addresses. */
    PARTIAL("partial"),

    /** Energy was delivered to no address. */
    FAILED("failed"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    BulkStatus(String value) {
        this.value = value;
    }

    /**
     * Returns the value as the API encodes it.
     *
     * @return the wire value, or an empty string for {@link #UNKNOWN}
     */
    public String value() {
        return value;
    }

    /**
     * Returns the constant for a wire value.
     *
     * @param value the value as the API encodes it, may be {@code null}
     * @return the matching constant, or {@link #UNKNOWN} when there is none
     */
    public static BulkStatus fromValue(String value) {
        for (BulkStatus candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
