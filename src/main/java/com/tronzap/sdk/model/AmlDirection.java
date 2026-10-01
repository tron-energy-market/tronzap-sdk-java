package com.tronzap.sdk.model;

/**
 * The direction of a transaction screened by an {@link AmlType#HASH} check.
 */
public enum AmlDirection {

    /** An incoming transaction. This is the API default. */
    DEPOSIT("deposit"),

    /** An outgoing transaction. */
    WITHDRAWAL("withdrawal"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    AmlDirection(String value) {
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
    public static AmlDirection fromValue(String value) {
        for (AmlDirection candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
