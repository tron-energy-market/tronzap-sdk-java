package com.tronzap.sdk.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One signal that contributed to an AML risk score.
 *
 * @param name the machine-readable factor name
 * @param label the human-readable factor name
 * @param group the risk group the factor belongs to, such as {@code low} or {@code medium}
 * @param score the weight of the factor, from 0 to 1
 */
public record AmlRiskFactor(String name, String label, String group, BigDecimal score) {

    /**
     * Validates the components.
     *
     * @param name the machine-readable factor name
     * @param label the human-readable factor name
     * @param group the risk group the factor belongs to, such as {@code low} or {@code medium}
     * @param score the weight of the factor, from 0 to 1
     */
    public AmlRiskFactor {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(score, "score");
    }
}
