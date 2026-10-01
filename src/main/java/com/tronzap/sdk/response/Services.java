package com.tronzap.sdk.response;

import com.tronzap.sdk.model.ActivateAddressRate;
import com.tronzap.sdk.model.BandwidthRate;
import com.tronzap.sdk.model.EnergyRate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The resources on sale and their prices.
 *
 * @param energy one price tier per energy amount range
 * @param bandwidth one price tier per bandwidth amount range
 * @param activateAddress the price of an address activation, empty when the API did not report one
 */
public record Services(List<EnergyRate> energy, List<BandwidthRate> bandwidth, Optional<ActivateAddressRate> activateAddress) {

    /**
     * Validates the components and copies the lists.
     *
     * @param energy one price tier per energy amount range
     * @param bandwidth one price tier per bandwidth amount range
     * @param activateAddress the price of an address activation, empty when the API did not report one
     */
    public Services {
        energy = List.copyOf(energy);
        bandwidth = List.copyOf(bandwidth);
        Objects.requireNonNull(activateAddress, "activateAddress");
    }
}
