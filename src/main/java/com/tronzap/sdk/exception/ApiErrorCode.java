package com.tronzap.sdk.exception;

/**
 * API error codes returned in the {@code code} field of an API response.
 *
 * <p>A successful response always carries code {@code 0}; any other value is reported as an {@link
 * ApiException}.
 */
public enum ApiErrorCode {

    /** Authentication error: the API token or the request signature is not valid. */
    AUTH_ERROR(1),

    /** Invalid service or parameters: check the service name and its parameters. */
    INVALID_SERVICE_OR_PARAMS(2),

    /** Internal wallet not found. Contact support. */
    WALLET_NOT_FOUND(5),

    /** Insufficient funds: top up the account balance or request a smaller amount. */
    INSUFFICIENT_FUNDS(6),

    /**
     * Invalid TRON address: it must be a valid 34-character TRON address. Starting a subscription also
     * reports this code when the address already has an active subscription.
     */
    INVALID_TRON_ADDRESS(10),

    /** Invalid energy amount. */
    INVALID_ENERGY_AMOUNT(11),

    /** Invalid duration. */
    INVALID_DURATION(12),

    /**
     * Transaction or subscription not found: check the transaction ID or external ID. The API reports
     * this code under the key {@code subscription_not_found}.
     */
    TRANSACTION_NOT_FOUND(20),

    /** The subscription cannot be stopped, for example because it has a transactions limit. */
    CANNOT_STOP_SUBSCRIPTION(21),

    /** Address not activated: activate it first with an address activation transaction. */
    ADDRESS_NOT_ACTIVATED(24),

    /** Address already activated. No action is needed. */
    ADDRESS_ALREADY_ACTIVATED(25),

    /** AML check not found: check the ID or run the AML check again. */
    AML_CHECK_NOT_FOUND(30),

    /** The service is temporarily unavailable. */
    SERVICE_NOT_AVAILABLE(35),

    /** Invalid bandwidth amount. */
    INVALID_BANDWIDTH_AMOUNT(50),

    /** Internal server error. Contact support if it persists. */
    INTERNAL_SERVER_ERROR(500),

    /** A code this version of the SDK does not know. Read the numeric value from {@link ApiException#getCode()}. */
    UNKNOWN(-1);

    private final int code;

    ApiErrorCode(int code) {
        this.code = code;
    }

    /**
     * Returns the numeric code as sent by the API.
     *
     * @return the numeric code, or {@code -1} for {@link #UNKNOWN}
     */
    public int code() {
        return code;
    }

    /**
     * Returns the constant for a numeric code.
     *
     * @param code the numeric code sent by the API
     * @return the matching constant, or {@link #UNKNOWN} when there is none
     */
    public static ApiErrorCode fromCode(int code) {
        for (ApiErrorCode value : values()) {
            if (value.code == code && value != UNKNOWN) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
