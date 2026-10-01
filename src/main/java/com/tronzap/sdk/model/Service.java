package com.tronzap.sdk.model;

/**
 * A service sold through the transaction endpoint.
 */
public enum Service {

    /** Energy rental. */
    ENERGY("energy"),

    /** Bandwidth rental. */
    BANDWIDTH("bandwidth"),

    /** Energy and bandwidth bought in one transaction. */
    RESOURCE_BUNDLE("resource_bundle"),

    /** Activation of a TRON address. */
    ACTIVATE_ADDRESS("activate_address"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    Service(String value) {
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
    public static Service fromValue(String value) {
        for (Service candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
