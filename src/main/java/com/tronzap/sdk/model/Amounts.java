package com.tronzap.sdk.model;

/**
 * The per-resource amounts of a purchase. A resource that is not part of the purchase is {@code 0}.
 *
 * @param energy the energy amount
 * @param bandwidth the bandwidth amount
 */
public record Amounts(long energy, long bandwidth) {
}
