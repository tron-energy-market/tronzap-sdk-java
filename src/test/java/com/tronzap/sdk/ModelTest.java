package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tronzap.sdk.exception.ApiErrorCode;
import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlRiskLevel;
import com.tronzap.sdk.model.AmlStatus;
import com.tronzap.sdk.model.AmlType;
import com.tronzap.sdk.model.Service;
import com.tronzap.sdk.model.Timestamp;
import com.tronzap.sdk.model.TransactionStatus;
import com.tronzap.sdk.request.AmlHistoryRequest;
import com.tronzap.sdk.request.CheckTransactionRequest;
import com.tronzap.sdk.request.EnergyTransactionRequest;
import com.tronzap.sdk.response.AmlHistory;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ModelTest {

    @ParameterizedTest
    @CsvSource({
        "2026-08-07T10:42:12+00:00, 2026-08-07T10:42:12Z",
        "2026-08-07T10:42:12Z, 2026-08-07T10:42:12Z",
        "2026-08-07T13:42:12+03:00, 2026-08-07T10:42:12Z",
        "2026-08-07T10:42:12.123456Z, 2026-08-07T10:42:12.123456Z",
        "2026-08-07T10:42:12, 2026-08-07T10:42:12Z",
        "2026-08-07 10:42:12, 2026-08-07T10:42:12Z",
        "2026-08-07 10:42:12.5, 2026-08-07T10:42:12.5Z",
        "2026-08-07, 2026-08-07T00:00:00Z",
        "1786099332, 2026-08-07T10:42:12Z"
    })
    void parsesTimestampEncodings(String raw, String expectedUtc) {
        Timestamp timestamp = Timestamp.parse(raw);

        assertEquals(raw, timestamp.raw());
        assertEquals(OffsetDateTime.parse(expectedUtc).toInstant(), timestamp.value().orElseThrow().toInstant());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "yesterday", "07/08/2026", "12345678901234"})
    void keepsUnparseableTimestampRaw(String raw) {
        Timestamp timestamp = Timestamp.parse(raw);

        assertEquals(raw, timestamp.raw());
        assertTrue(timestamp.value().isEmpty());
    }

    @Test
    void timestampWithoutOffsetIsUtc() {
        assertEquals(ZoneOffset.UTC, Timestamp.parse("2026-08-07 10:42:12").value().orElseThrow().getOffset());
    }

    @Test
    void enumsRoundTripTheirWireValues() {
        for (Service value : Service.values()) {
            if (value != Service.UNKNOWN) {
                assertSame(value, Service.fromValue(value.value()));
            }
        }
        assertEquals("resource_bundle", Service.RESOURCE_BUNDLE.value());
        assertSame(TransactionStatus.SUCCESS, TransactionStatus.fromValue("success"));
        assertSame(AmlType.HASH, AmlType.fromValue("hash"));
        assertSame(AmlDirection.DEPOSIT, AmlDirection.fromValue("deposit"));
        assertSame(AmlStatus.COMPLETED, AmlStatus.fromValue("completed"));
        assertSame(AmlRiskLevel.HIGH, AmlRiskLevel.fromValue("high"));
    }

    @Test
    void unknownEnumValues() {
        assertSame(Service.UNKNOWN, Service.fromValue("teleport"));
        assertSame(Service.UNKNOWN, Service.fromValue(null));
        assertSame(Service.UNKNOWN, Service.fromValue(""));
        assertSame(TransactionStatus.UNKNOWN, TransactionStatus.fromValue("SUCCESS"));
        assertSame(AmlRiskLevel.UNKNOWN, AmlRiskLevel.fromValue("critical"));
    }

    @Test
    void errorCodes() {
        assertSame(ApiErrorCode.AUTH_ERROR, ApiErrorCode.fromCode(1));
        assertSame(ApiErrorCode.TRANSACTION_NOT_FOUND, ApiErrorCode.fromCode(20));
        assertSame(ApiErrorCode.INVALID_BANDWIDTH_AMOUNT, ApiErrorCode.fromCode(50));
        assertSame(ApiErrorCode.UNKNOWN, ApiErrorCode.fromCode(-1));
        assertSame(ApiErrorCode.UNKNOWN, ApiErrorCode.fromCode(0));
        assertEquals(500, ApiErrorCode.INTERNAL_SERVER_ERROR.code());
    }

    @Test
    void requestDefaults() {
        EnergyTransactionRequest energy = EnergyTransactionRequest.of("TAddress", 65000);
        AmlHistoryRequest history = AmlHistoryRequest.firstPage();

        assertEquals(1, energy.duration());
        assertTrue(energy.externalId().isEmpty());
        assertEquals(false, energy.activateAddress());
        assertEquals(1, history.page());
        assertEquals(10, history.perPage());
        assertTrue(history.status().isEmpty());
    }

    @Test
    void requestsAreValueObjects() {
        assertEquals(CheckTransactionRequest.byId("tx-1"), CheckTransactionRequest.byId("tx-1"));
        assertEquals(EnergyTransactionRequest.of("TAddress", 65000), EnergyTransactionRequest.of("TAddress", 65000));
    }

    @Test
    void responsesCopyTheirCollections() {
        List<com.tronzap.sdk.response.AmlCheck> items = new ArrayList<>();
        AmlHistory history = new AmlHistory(1, 10, 0, items);
        items.add(null);

        assertTrue(history.items().isEmpty());
        assertThrows(NullPointerException.class, () -> new AmlHistory(1, 10, 0, null));
    }
}
