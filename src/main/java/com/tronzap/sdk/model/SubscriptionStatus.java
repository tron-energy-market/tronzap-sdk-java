package com.tronzap.sdk.model;

/**
 * The status of a subscription.
 */
public enum SubscriptionStatus {

    /** The subscription was created but has not started yet. */
    NEW("new"),

    /** The subscription is being started. */
    PENDING("pending"),

    /** The subscription could not be started. */
    ERROR("error"),

    /** The subscription is delegating energy. */
    ACTIVE("active"),

    /** The subscription was stopped. */
    STOPPED("stopped"),

    /** The subscription ran out of time or transactions. */
    EXPIRED("expired"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    SubscriptionStatus(String value) {
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
    public static SubscriptionStatus fromValue(String value) {
        for (SubscriptionStatus candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
