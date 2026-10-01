package com.tronzap.sdk.model;

/**
 * The risk level assigned by a completed AML check.
 */
public enum AmlRiskLevel {

    /** Low risk. */
    LOW("low"),

    /** Medium risk. */
    MEDIUM("medium"),

    /** High risk. */
    HIGH("high"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    AmlRiskLevel(String value) {
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
    public static AmlRiskLevel fromValue(String value) {
        for (AmlRiskLevel candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
