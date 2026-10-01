package com.tronzap.sdk.model;

/**
 * The available amounts of each TRON resource on an address.
 *
 * @param energy the available energy
 * @param bandwidth the available bandwidth
 */
public record Resources(long energy, long bandwidth) {
}
