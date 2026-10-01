package com.tronzap.sdk.model;

/**
 * What an AML check screens.
 */
public enum AmlType {

    /** Screens a wallet address. */
    ADDRESS("address"),

    /** Screens a transaction hash. */
    HASH("hash"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    AmlType(String value) {
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
    public static AmlType fromValue(String value) {
        for (AmlType candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
