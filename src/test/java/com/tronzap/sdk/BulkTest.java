package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tronzap.sdk.exception.ApiErrorCode;
import com.tronzap.sdk.exception.ApiException;
import com.tronzap.sdk.exception.BulkNoValidItemsException;
import com.tronzap.sdk.exception.BulkPriceChangedException;
import com.tronzap.sdk.exception.InvalidResponseException;
import com.tronzap.sdk.model.BulkRejectedRow;
import com.tronzap.sdk.model.BulkRowStatus;
import com.tronzap.sdk.model.BulkStatus;
import com.tronzap.sdk.request.BulkCalculateRequest;
import com.tronzap.sdk.request.BulkCreateRequest;
import com.tronzap.sdk.request.BulkHistoryRequest;
import com.tronzap.sdk.request.BulkItem;
import com.tronzap.sdk.request.BulkOrderRequest;
import com.tronzap.sdk.request.BulkReportRequest;
import com.tronzap.sdk.response.BulkCalculation;
import com.tronzap.sdk.response.BulkHistory;
import com.tronzap.sdk.response.BulkOrder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class BulkTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String CALCULATION = "{\"id\":\"01kb2m7a1bc2de3fg4hj5km6np\",\"status\":\"draft\","
            + "\"resource\":\"energy\",\"duration\":1,\"total_amount\":1240.1,\"accepted\":2,\"rejected_count\":1,"
            + "\"rejected\":[{\"row\":3,\"address\":\"abc\",\"reason\":\"invalid_address\"}],"
            + "\"expires_at\":\"2026-11-16T12:15:00+00:00\"}";

    private static final String ORDER = "{\"id\":\"01kb2m5x9qj4v8n3c7d6e5f4g3\",\"external_id\":\"my-order-42\","
            + "\"status\":\"partial\",\"resource\":\"energy\",\"duration\":1,\"force\":true,\"max_price_increase\":20,"
            + "\"webhook_url\":\"https://example.com/tronzap/bulk\",\"total\":4,\"processed\":4,\"delivered\":2,"
            + "\"failed\":0,\"in_review\":0,\"skipped\":2,"
            + "\"skipped_by_reason\":{\"address_not_activated\":1,\"price_increased\":1},\"rejected_count\":1,"
            + "\"total_amount\":\"1240.10\",\"charged_amount\":\"620.05\",\"created_at\":\"2026-11-16T12:05:00+00:00\","
            + "\"started_at\":\"2026-11-16T12:06:00+00:00\",\"finalized_at\":\"2026-11-16T12:09:00+00:00\"}";

    private static final String CSV = "row,address,amount,status,reason,charged,transaction_id\n"
            + "1,TPY1Kb8cKAZQfm95gXQQs2Mh8Uygtos21D,65000,delivered,,3.25,01kb2m9c4de5fg6hj7km8np9qr\n"
            + "2,TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE,131000,skipped,address_not_activated,,\n";

    private static void assertJson(String expected, String actual) throws Exception {
        assertEquals(JSON.readTree(expected), JSON.readTree(actual));
    }

    @Test
    void calculateSendsEnergyAndTheRows() throws Exception {
        try (TestServer server = TestServer.start().replyOk(CALCULATION)) {
            BulkCalculation calculation = server.client().bulkCalculate(new BulkCalculateRequest(
                    List.of(BulkItem.of("TAddress1", 65000), BulkItem.of("TAddress2", 131000)), 24));

            TestServer.Received request = server.onlyRequest();
            assertEquals("/v1/bulk/calculate", request.path());
            assertJson("{\"resource\":\"energy\",\"duration\":24,\"items\":["
                    + "{\"address\":\"TAddress1\",\"amount\":65000},{\"address\":\"TAddress2\",\"amount\":131000}]}",
                    request.bodyText());

            assertEquals("01kb2m7a1bc2de3fg4hj5km6np", calculation.id());
            assertSame(BulkStatus.DRAFT, calculation.status());
            assertEquals("energy", calculation.resource());
            assertEquals(1, calculation.duration());
            assertEquals(0, new BigDecimal("1240.1").compareTo(calculation.totalAmount()));
            assertEquals(2, calculation.accepted());
            assertEquals(1, calculation.rejectedCount());
            assertEquals(List.of(new BulkRejectedRow(3, "abc", "invalid_address")), calculation.rejected());
            assertEquals("2026-11-16T12:15:00+00:00", calculation.expiresAt().orElseThrow().raw());
        }
    }

    @Test
    void calculateDefaultsToOneHourAndSendsInvalidRowsAsTheyAre() throws Exception {
        try (TestServer server = TestServer.start().replyOk(CALCULATION)) {
            server.client().bulkCalculate(BulkCalculateRequest.of(List.of(BulkItem.of("", -1))));

            assertJson("{\"resource\":\"energy\",\"duration\":1,\"items\":[{\"address\":\"\",\"amount\":-1}]}",
                    server.onlyRequest().bodyText());
        }
    }

    @Test
    void calculateWithNoValidRowsReportsThem() throws Exception {
        String body = "{\"code\":94,\"key\":\"bulk_no_valid_items\",\"error\":\"No valid items\","
                + "\"result\":{\"rejected\":[{\"row\":1,\"address\":\"abc\",\"reason\":\"invalid_address\"}]}}";
        try (TestServer server = TestServer.start().reply(200, body)) {
            BulkNoValidItemsException e = assertThrows(BulkNoValidItemsException.class,
                    () -> server.client().bulkCalculate(BulkCalculateRequest.of(List.of(BulkItem.of("abc", 65000)))));

            assertSame(ApiErrorCode.BULK_NO_VALID_ITEMS, e.getErrorCode());
            assertEquals(List.of(new BulkRejectedRow(1, "abc", "invalid_address")), e.getRejected());
            assertJson("{\"rejected\":[{\"row\":1,\"address\":\"abc\",\"reason\":\"invalid_address\"}]}",
                    e.getResult().orElseThrow());
        }
    }

    @Test
    void createSendsOnlyTheGivenOptions() throws Exception {
        try (TestServer server = TestServer.start().replyOk(ORDER)) {
            server.client().bulkCreate(BulkCreateRequest.of("calc-1"));

            TestServer.Received request = server.onlyRequest();
            assertEquals("/v1/bulk/create", request.path());
            assertJson("{\"id\":\"calc-1\"}", request.bodyText());
        }
    }

    @Test
    void createWithAllOptions() throws Exception {
        try (TestServer server = TestServer.start().replyOk(ORDER)) {
            BulkOrder order = server.client().bulkCreate(BulkCreateRequest.builder("calc-1")
                    .externalId("my-order-42")
                    .force(true)
                    .maxPriceIncrease(0)
                    .webhookUrl("https://example.com/tronzap/bulk")
                    .build());

            assertJson("{\"id\":\"calc-1\",\"force\":true,\"max_price_increase\":0,"
                    + "\"webhook_url\":\"https://example.com/tronzap/bulk\",\"external_id\":\"my-order-42\"}",
                    server.onlyRequest().bodyText());
            assertEquals("01kb2m5x9qj4v8n3c7d6e5f4g3", order.id());
        }
    }

    @Test
    void createReturnsTheNewCalculationWhenThePriceChanged() throws Exception {
        String body = "{\"code\":92,\"key\":\"bulk_price_changed\",\"error\":\"Price changed, confirmation required\","
                + "\"request_id\":\"req-92\",\"result\":" + CALCULATION + "}";
        try (TestServer server = TestServer.start().reply(200, body)) {
            ApiException e = assertThrows(ApiException.class,
                    () -> server.client().bulkCreate(BulkCreateRequest.of("calc-old")));

            BulkPriceChangedException changed = assertInstanceOf(BulkPriceChangedException.class, e);
            assertSame(ApiErrorCode.BULK_PRICE_CHANGED, changed.getErrorCode());
            assertEquals("bulk_price_changed", changed.getErrorKey().orElseThrow());
            assertEquals("req-92", changed.getRequestId().orElseThrow());
            assertEquals("01kb2m7a1bc2de3fg4hj5km6np", changed.getCalculation().id());
            assertJson(CALCULATION, changed.getResult().orElseThrow());
        }
    }

    @Test
    void errorDataOfAnUnexpectedShapeIsStillTheApiError() throws Exception {
        String body = "{\"code\":92,\"error\":\"Price changed\",\"result\":{\"accepted\":\"many\"}}";
        try (TestServer server = TestServer.start().reply(200, body)) {
            ApiException e = assertThrows(ApiException.class,
                    () -> server.client().bulkCreate(BulkCreateRequest.of("calc-old")));

            assertSame(ApiException.class, e.getClass());
            assertSame(ApiErrorCode.BULK_PRICE_CHANGED, e.getErrorCode());
            assertJson("{\"accepted\":\"many\"}", e.getResult().orElseThrow());
        }
        try (TestServer server = TestServer.start().reply(200, "{\"code\":94,\"error\":\"No valid items\",\"result\":[]}")) {
            ApiException e = assertThrows(ApiException.class,
                    () -> server.client().bulkCalculate(BulkCalculateRequest.of(List.of(BulkItem.of("abc", 1)))));

            assertSame(ApiException.class, e.getClass());
            assertEquals("[]", e.getResult().orElseThrow());
        }
    }

    @Test
    void checkByIdOrExternalId() throws Exception {
        try (TestServer server = TestServer.start().replyOk(ORDER)) {
            TronzapClient client = server.client();
            BulkOrder order = client.bulkCheck(BulkOrderRequest.byId("order-1"));
            client.bulkCheck(BulkOrderRequest.byExternalId("my-order"));

            List<TestServer.Received> requests = server.requests();
            assertEquals("/v1/bulk/check", requests.get(0).path());
            assertJson("{\"id\":\"order-1\"}", requests.get(0).bodyText());
            assertJson("{\"external_id\":\"my-order\"}", requests.get(1).bodyText());

            assertEquals(Optional.of("my-order-42"), order.externalId());
            assertSame(BulkStatus.PARTIAL, order.status());
            assertEquals("energy", order.resource());
            assertEquals(1, order.duration());
            assertTrue(order.force());
            assertEquals(20, order.maxPriceIncrease());
            assertEquals(Optional.of("https://example.com/tronzap/bulk"), order.webhookUrl());
            assertEquals(4, order.total());
            assertEquals(4, order.processed());
            assertEquals(2, order.delivered());
            assertEquals(0, order.failed());
            assertEquals(0, order.inReview());
            assertEquals(2, order.skipped());
            assertEquals(Map.of("address_not_activated", 1, "price_increased", 1), order.skippedByReason());
            assertEquals(1, order.rejectedCount());
            assertEquals(new BigDecimal("1240.10"), order.totalAmount());
            assertEquals(new BigDecimal("620.05"), order.chargedAmount());
            assertEquals("2026-11-16T12:05:00+00:00", order.createdAt().orElseThrow().raw());
            assertEquals("2026-11-16T12:06:00+00:00", order.startedAt().orElseThrow().raw());
            assertEquals("2026-11-16T12:09:00+00:00", order.finalizedAt().orElseThrow().raw());
        }
    }

    @Test
    void processingOrderHasNoFinalDateAndNoSkips() throws Exception {
        String processing = "{\"id\":\"order-1\",\"external_id\":null,\"status\":\"processing\",\"webhook_url\":null,"
                + "\"skipped_by_reason\":{},\"finalized_at\":null}";
        try (TestServer server = TestServer.start().replyOk(processing)) {
            BulkOrder order = server.client().bulkCheck(BulkOrderRequest.byId("order-1"));

            assertSame(BulkStatus.PROCESSING, order.status());
            assertTrue(order.externalId().isEmpty());
            assertTrue(order.webhookUrl().isEmpty());
            assertTrue(order.skippedByReason().isEmpty());
            assertTrue(order.finalizedAt().isEmpty());
        }
    }

    @Test
    void unknownOrderIsAnApiError() throws Exception {
        try (TestServer server = TestServer.start()
                .reply(200, "{\"code\":90,\"key\":\"bulk_not_found\",\"error\":\"Bulk order not found\"}")) {
            ApiException e = assertThrows(ApiException.class,
                    () -> server.client().bulkCheck(BulkOrderRequest.byExternalId("missing")));

            assertSame(ApiErrorCode.BULK_NOT_FOUND, e.getErrorCode());
            assertTrue(e.getResult().isEmpty());
        }
    }

    @Test
    void historyPaging() throws Exception {
        try (TestServer server = TestServer.start()
                .replyOk("{\"page\":3,\"per_page\":50,\"total\":101,\"items\":[" + ORDER + "]}")) {
            TronzapClient client = server.client();
            BulkHistory history = client.getBulkHistory(BulkHistoryRequest.of(3, 50));
            client.getBulkHistory();

            List<TestServer.Received> requests = server.requests();
            assertEquals("/v1/bulk/history", requests.get(0).path());
            assertJson("{\"page\":3,\"per_page\":50}", requests.get(0).bodyText());
            assertJson("{\"page\":1,\"per_page\":10}", requests.get(1).bodyText());

            assertEquals(3, history.page());
            assertEquals(50, history.perPage());
            assertEquals(101, history.total());
            assertEquals("01kb2m5x9qj4v8n3c7d6e5f4g3", history.items().get(0).id());
        }
    }

    @Test
    void reportReturnsTheCsv() throws Exception {
        try (TestServer server = TestServer.start()
                .reply(200, CSV, Map.of("Content-Type", "text/csv; charset=UTF-8"))) {
            String report = server.client().bulkReport(BulkReportRequest.byId("order-1").withStatus(BulkRowStatus.SKIPPED));

            assertEquals(CSV, report);
            TestServer.Received request = server.onlyRequest();
            assertEquals("/v1/bulk/report", request.path());
            assertJson("{\"id\":\"order-1\",\"status\":\"skipped\"}", request.bodyText());
        }
    }

    @Test
    void reportIsUtf8WithoutACharset() throws Exception {
        String csv = "row,reason\n1,адреса не активована\n";
        try (TestServer server = TestServer.start().reply(200, csv, Map.of("Content-Type", "text/csv"))) {
            assertEquals(csv, server.client().bulkReport(BulkReportRequest.byExternalId("my-order")));
            assertJson("{\"external_id\":\"my-order\"}", server.onlyRequest().bodyText());
        }
    }

    @Test
    void reportErrorsAreJson() throws Exception {
        try (TestServer server = TestServer.start()
                .reply(200, "{\"code\":90,\"key\":\"bulk_not_found\",\"error\":\"Bulk order not found\"}")) {
            ApiException e = assertThrows(ApiException.class,
                    () -> server.client().bulkReport(BulkReportRequest.byExternalId("missing")));

            assertSame(ApiErrorCode.BULK_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    void reportThatIsNotCsvIsAnInvalidResponse() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{\"rows\":[]}")) {
            InvalidResponseException e = assertThrows(InvalidResponseException.class,
                    () -> server.client().bulkReport(BulkReportRequest.byId("order-1")));

            assertEquals("expected a CSV report", e.getMessage());
        }
    }

    @Test
    void webhookSignature() throws Exception {
        String body = "{\"id\":\"01kb2m5x9qj4v8n3c7d6e5f4g3\",\"external_id\":\"my-order-42\",\"status\":\"partial\"}";
        String signature = sha256(body + TestServer.SECRET);
        TronzapClient client = TronzapClient.builder().apiToken(TestServer.TOKEN).apiSecret(TestServer.SECRET).build();

        assertTrue(client.verifyWebhookSignature(body, signature));
        assertTrue(client.verifyWebhookSignature(body.getBytes(StandardCharsets.UTF_8), signature));
        assertTrue(client.verifyWebhookSignature(body, " " + signature.toUpperCase() + "\n"));
        assertFalse(client.verifyWebhookSignature(body + " ", signature));
        assertFalse(client.verifyWebhookSignature(body, ""));
        assertFalse(client.verifyWebhookSignature(body, null));
        assertFalse(client.verifyWebhookSignature(body, "підпис"));
        assertFalse(client.verifyWebhookSignature(body, sha256(body + "other-secret")));
    }

    @Test
    void requestsAreValidatedBeforeSending() {
        assertThrows(IllegalArgumentException.class, () -> BulkCalculateRequest.of(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new BulkCalculateRequest(List.of(BulkItem.of("T", 1)), 0));
        assertThrows(NullPointerException.class, () -> BulkItem.of(null, 1));
        List<BulkItem> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> BulkCalculateRequest.of(withNull));
        assertThrows(IllegalArgumentException.class, () -> BulkCreateRequest.of(" "));
        assertThrows(IllegalArgumentException.class, () -> BulkCreateRequest.builder("calc-1").externalId("").build());
        assertThrows(IllegalArgumentException.class, () -> BulkCreateRequest.builder("calc-1").webhookUrl(" ").build());
        assertThrows(IllegalArgumentException.class, () -> new BulkOrderRequest(Optional.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> BulkOrderRequest.byId(""));
        assertThrows(IllegalArgumentException.class, () -> BulkOrderRequest.byExternalId(null));
        assertThrows(IllegalArgumentException.class,
                () -> new BulkReportRequest(Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> BulkReportRequest.byId(" "));
        assertThrows(IllegalArgumentException.class, () -> BulkReportRequest.byExternalId(""));
        assertThrows(IllegalArgumentException.class, () -> BulkReportRequest.byId("order-1").withStatus(BulkRowStatus.UNKNOWN));
        assertThrows(IllegalArgumentException.class, () -> BulkReportRequest.byId("order-1").withStatus(null));
        assertThrows(IllegalArgumentException.class, () -> BulkHistoryRequest.of(0, 10));
        assertThrows(IllegalArgumentException.class, () -> BulkHistoryRequest.of(1, 0));
    }

    @Test
    void requestDefaults() {
        BulkCreateRequest create = BulkCreateRequest.of("calc-1");
        assertTrue(create.externalId().isEmpty());
        assertFalse(create.force());
        assertEquals(OptionalInt.empty(), create.maxPriceIncrease());
        assertTrue(create.webhookUrl().isEmpty());
        assertEquals(1, BulkCalculateRequest.of(List.of(BulkItem.of("T", 1))).duration());
        assertEquals(new BulkHistoryRequest(1, BulkHistoryRequest.DEFAULT_PER_PAGE), BulkHistoryRequest.firstPage());
    }

    @Test
    void bulkStatusesRoundTripTheirWireValues() {
        for (BulkStatus value : BulkStatus.values()) {
            if (value != BulkStatus.UNKNOWN) {
                assertSame(value, BulkStatus.fromValue(value.value()));
            }
        }
        for (BulkRowStatus value : BulkRowStatus.values()) {
            if (value != BulkRowStatus.UNKNOWN) {
                assertSame(value, BulkRowStatus.fromValue(value.value()));
            }
        }
        assertEquals("in_review", BulkRowStatus.IN_REVIEW.value());
        assertSame(BulkStatus.UNKNOWN, BulkStatus.fromValue("paused"));
        assertSame(BulkRowStatus.UNKNOWN, BulkRowStatus.fromValue(null));
    }

    private static String sha256(String text) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }
}
