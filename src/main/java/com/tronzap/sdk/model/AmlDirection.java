package com.tronzap.sdk.model;

/**
 * Which side of the transaction you are on in an {@link AmlType#HASH} check. The risk is scored for
 * the counterparty. When a hash check gives no direction, the SDK sends {@link #DEPOSIT}.
 */
public enum AmlDirection {

    /**
     * The funds were sent to your address: the check's address is your address and the sender is
     * scored.
     */
    DEPOSIT("deposit"),

    /**
     * You sent the funds: the check's address is the external recipient's address and the recipient
     * is scored.
     */
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
