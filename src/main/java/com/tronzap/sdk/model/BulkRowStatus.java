package com.tronzap.sdk.model;

/**
 * The result of one address of a bulk order: the {@code status} column of the bulk report.
 */
public enum BulkRowStatus {

    /** Energy was delivered. */
    DELIVERED("delivered"),

    /** Energy was not delivered and the charge was refunded. */
    FAILED("failed"),

    /** Support is checking the address. */
    IN_REVIEW("in_review"),

    /** The address was not bought and not charged. */
    SKIPPED("skipped"),

    /** The address was not processed yet. */
    PENDING("pending"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    BulkRowStatus(String value) {
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
    public static BulkRowStatus fromValue(String value) {
        for (BulkRowStatus candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
