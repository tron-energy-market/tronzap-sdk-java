package com.tronzap.sdk.model;

/**
 * The status of an AML check.
 */
public enum AmlStatus {

    /** The check is queued. */
    PENDING("pending"),

    /** The check is running. */
    PROCESSING("processing"),

    /** The check finished and its results are available. */
    COMPLETED("completed"),

    /** The check could not be completed. */
    FAILED("failed"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    AmlStatus(String value) {
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
    public static AmlStatus fromValue(String value) {
        for (AmlStatus candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
