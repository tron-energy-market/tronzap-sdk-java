/**
 * Immutable results returned by {@link com.tronzap.sdk.TronzapClient}.
 *
 * <p>Collections are never {@code null}: an absent list or map is empty. Values the API may omit are
 * {@link java.util.Optional}. Monetary values are {@link java.math.BigDecimal} with the scale the API
 * sent, so compare them with {@link java.math.BigDecimal#compareTo(java.math.BigDecimal)} rather than
 * {@code equals}.
 */
package com.tronzap.sdk.response;
