package com.tronzap.sdk.response;

import com.tronzap.sdk.model.AmlType;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * One AML screening product and its price.
 *
 * @param id the service identifier
 * @param type what the service screens
 * @param price the cost of a single check
 */
public record AmlService(String id, AmlType type, BigDecimal price) {

    /**
     * Validates the components.
     *
     * @param id the service identifier
     * @param type what the service screens
     * @param price the cost of a single check
     */
    public AmlService {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(price, "price");
    }
}
