package com.tronzap.sdk.model;

/**
 * The status of a transaction. A transaction moves from {@link #NEW} to {@link #PENDING} and then to {@link #SUCCESS} or {@link #FAILED}.
 */
public enum TransactionStatus {

    /** The transaction was created but processing has not started. */
    NEW("new"),

    /** The transaction is being processed. */
    PENDING("pending"),

    /** The transaction completed successfully. */
    SUCCESS("success"),

    /** The transaction failed. */
    FAILED("failed"),

    /** A value this version of the SDK does not know. */
    UNKNOWN("");

    private final String value;

    TransactionStatus(String value) {
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
    public static TransactionStatus fromValue(String value) {
        for (TransactionStatus candidate : values()) {
            if (candidate != UNKNOWN && candidate.value.equals(value)) {
                return candidate;
            }
        }
        return UNKNOWN;
    }
}
