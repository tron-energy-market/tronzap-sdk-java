package com.tronzap.sdk.response;

import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlRiskFactor;
import com.tronzap.sdk.model.AmlRiskLevel;
import com.tronzap.sdk.model.AmlStatus;
import com.tronzap.sdk.model.AmlType;
import com.tronzap.sdk.model.Timestamp;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An AML screening and, once completed, its result.
 *
 * <p>{@link #riskScore()} is empty until the check completes. A completed check can have a score of
 * {@code 0}, which is different from having no score yet.
 *
 * @param id the check identifier
 * @param type what was screened
 * @param address the screened address; for a hash check, the recipient address of the transaction
 * @param hash the screened transaction hash, empty for address checks
 * @param direction the screened transaction direction, empty for address checks
 * @param network the blockchain network code, for example {@code TRX}, {@code BTC} or {@code ETH}
 * @param status the current status
 * @param riskScore the risk score from 0 to 100, empty until the check completes
 * @param riskLevel the risk level, empty until the check completes
 * @param blacklist whether the subject appears on a blacklist
 * @param riskFactors the signals that contributed to the score
 * @param checkedAt when the screening ran, empty when the API did not report it
 */
public record AmlCheck(
        String id,
        AmlType type,
        String address,
        Optional<String> hash,
        Optional<AmlDirection> direction,
        String network,
        AmlStatus status,
        Optional<BigDecimal> riskScore,
        Optional<AmlRiskLevel> riskLevel,
        boolean blacklist,
        List<AmlRiskFactor> riskFactors,
        Optional<Timestamp> checkedAt) {

    /**
     * Validates the components and copies the list.
     *
     * @param id the check identifier
     * @param type what was screened
     * @param address the screened address; for a hash check, the recipient address of the
     *     transaction
     * @param hash the screened transaction hash, empty for address checks
     * @param direction the screened transaction direction, empty for address checks
     * @param network the blockchain network code, for example {@code TRX}, {@code BTC} or {@code ETH}
     * @param status the current status
     * @param riskScore the risk score from 0 to 100, empty until the check completes
     * @param riskLevel the risk level, empty until the check completes
     * @param blacklist whether the subject appears on a blacklist
     * @param riskFactors the signals that contributed to the score
     * @param checkedAt when the screening ran, empty when the API did not report it
     */
    public AmlCheck {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(network, "network");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(riskScore, "riskScore");
        Objects.requireNonNull(riskLevel, "riskLevel");
        riskFactors = List.copyOf(riskFactors);
        Objects.requireNonNull(checkedAt, "checkedAt");
    }
}
