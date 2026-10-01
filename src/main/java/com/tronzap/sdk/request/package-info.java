/**
 * Immutable parameters for the {@link com.tronzap.sdk.TronzapClient} methods that take more than one
 * argument.
 *
 * <p>Each request validates itself on construction and throws {@link
 * java.lang.IllegalArgumentException} for a missing or out-of-range value, so an invalid request is
 * never sent. Create one with its {@code of} factory for the required values, or with its {@code
 * builder()} to set optional ones too.
 */
package com.tronzap.sdk.request;
