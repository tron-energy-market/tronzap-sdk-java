package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlStatus;
import com.tronzap.sdk.model.SubscriptionStatus;
import com.tronzap.sdk.request.AddressActivationRequest;
import com.tronzap.sdk.request.AmlCheckRequest;
import com.tronzap.sdk.request.AmlHistoryRequest;
import com.tronzap.sdk.request.BandwidthTransactionRequest;
import com.tronzap.sdk.request.CalculateRequest;
import com.tronzap.sdk.request.CheckTransactionRequest;
import com.tronzap.sdk.request.EnergyTransactionRequest;
import com.tronzap.sdk.request.EstimateEnergyRequest;
import com.tronzap.sdk.request.ResourceBundleTransactionRequest;
import com.tronzap.sdk.request.StartSubscriptionRequest;
import com.tronzap.sdk.request.SubscriptionHistoryRequest;
import com.tronzap.sdk.request.SubscriptionRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequestWireTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    static Stream<Arguments> endpoints() {
        return Stream.of(
                wire("getServices", "/v1/services", "{}", "{}", c -> c.getServices()),
                wire("getAmlServices", "/v1/aml-checks", "{}", "[]", c -> c.getAmlServices()),
                wire("getBalance", "/v1/balance", "{}", "{}", c -> c.getBalance()),
                wire("getAddressInfo", "/v1/address-info", "{\"address\":\"TAddress\"}", "{}",
                        c -> c.getAddressInfo("TAddress")),
                wire("estimateEnergy default contract", "/v1/estimate-energy",
                        "{\"from_address\":\"TFrom\",\"to_address\":\"TTo\"}", "{}",
                        c -> c.estimateEnergy(EstimateEnergyRequest.of("TFrom", "TTo"))),
                wire("estimateEnergy explicit contract", "/v1/estimate-energy",
                        "{\"from_address\":\"TFrom\",\"to_address\":\"TTo\",\"contract_address\":\"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\"}", "{}",
                        c -> c.estimateEnergy(EstimateEnergyRequest.of("TFrom", "TTo", EstimateEnergyRequest.USDT_CONTRACT_ADDRESS))),
                wire("calculate sends amount, not the deprecated energy field", "/v1/calculate",
                        "{\"address\":\"TAddress\",\"amount\":65000,\"duration\":1}", "{}",
                        c -> c.calculate(CalculateRequest.of("TAddress", 65000))),
                wire("calculate 24h", "/v1/calculate",
                        "{\"address\":\"TAddress\",\"amount\":65000,\"duration\":24}", "{}",
                        c -> c.calculate(new CalculateRequest("TAddress", 65000, 24))),
                wire("createEnergyTransaction all options", "/v1/transaction/new",
                        "{\"service\":\"energy\",\"external_id\":\"ext-1\",\"params\":{\"address\":\"TAddress\",\"amounts\":{\"energy\":65000},\"duration\":24,\"activate_address\":true}}", "{}",
                        c -> c.createEnergyTransaction(EnergyTransactionRequest.builder("TAddress", 65000)
                                .duration(24).externalId("ext-1").activateAddress(true).build())),
                wire("createEnergyTransaction defaults", "/v1/transaction/new",
                        "{\"service\":\"energy\",\"params\":{\"address\":\"TAddress\",\"amounts\":{\"energy\":65000},\"duration\":1}}", "{}",
                        c -> c.createEnergyTransaction(EnergyTransactionRequest.of("TAddress", 65000))),
                wire("createBandwidthTransaction", "/v1/transaction/new",
                        "{\"service\":\"bandwidth\",\"external_id\":\"bw-1\",\"params\":{\"address\":\"TAddress\",\"amounts\":{\"bandwidth\":345},\"duration\":1}}", "{}",
                        c -> c.createBandwidthTransaction(BandwidthTransactionRequest.of("TAddress", 345, "bw-1"))),
                wire("createBandwidthTransaction without external id", "/v1/transaction/new",
                        "{\"service\":\"bandwidth\",\"params\":{\"address\":\"TAddress\",\"amounts\":{\"bandwidth\":345},\"duration\":1}}", "{}",
                        c -> c.createBandwidthTransaction(BandwidthTransactionRequest.of("TAddress", 345))),
                wire("createResourceBundleTransaction", "/v1/transaction/new",
                        "{\"service\":\"resource_bundle\",\"external_id\":\"bundle-1\",\"params\":{\"address\":\"TAddress\",\"amounts\":{\"energy\":65000,\"bandwidth\":345},\"duration\":1,\"activate_address\":true}}", "{}",
                        c -> c.createResourceBundleTransaction(ResourceBundleTransactionRequest.builder("TAddress", 65000, 345)
                                .externalId("bundle-1").activateAddress(true).build())),
                wire("createResourceBundleTransaction defaults", "/v1/transaction/new",
                        "{\"service\":\"resource_bundle\",\"params\":{\"address\":\"TAddress\",\"amounts\":{\"energy\":65000,\"bandwidth\":345},\"duration\":1}}", "{}",
                        c -> c.createResourceBundleTransaction(ResourceBundleTransactionRequest.of("TAddress", 65000, 345))),
                wire("createAddressActivationTransaction", "/v1/transaction/new",
                        "{\"service\":\"activate_address\",\"external_id\":\"act-1\",\"params\":{\"address\":\"TAddress\"}}", "{}",
                        c -> c.createAddressActivationTransaction(AddressActivationRequest.of("TAddress", "act-1"))),
                wire("createAddressActivationTransaction without external id", "/v1/transaction/new",
                        "{\"service\":\"activate_address\",\"params\":{\"address\":\"TAddress\"}}", "{}",
                        c -> c.createAddressActivationTransaction(AddressActivationRequest.of("TAddress"))),
                wire("checkTransaction by id", "/v1/transaction/check", "{\"id\":\"tx-1\"}", "{}",
                        c -> c.checkTransaction(CheckTransactionRequest.byId("tx-1"))),
                wire("checkTransaction by external id", "/v1/transaction/check", "{\"external_id\":\"ext-1\"}", "{}",
                        c -> c.checkTransaction(CheckTransactionRequest.byExternalId("ext-1"))),
                wire("getDirectRechargeInfo", "/v1/direct-recharge-info", "{}", "{}", c -> c.getDirectRechargeInfo()),
                wire("createAmlCheck address", "/v1/aml-checks/new",
                        "{\"type\":\"address\",\"network\":\"TRX\",\"address\":\"TAddress\"}", "{}",
                        c -> c.createAmlCheck(AmlCheckRequest.forAddress("TRX", "TAddress"))),
                wire("createAmlCheck hash", "/v1/aml-checks/new",
                        "{\"type\":\"hash\",\"network\":\"BTC\",\"address\":\"bc1address\",\"hash\":\"E3F2\",\"direction\":\"withdrawal\"}", "{}",
                        c -> c.createAmlCheck(AmlCheckRequest.forHash("BTC", "bc1address", "E3F2", AmlDirection.WITHDRAWAL))),
                wire("checkAmlStatus", "/v1/aml-checks/check", "{\"id\":\"aml-1\"}", "{}", c -> c.checkAmlStatus("aml-1")),
                wire("getAmlHistory defaults", "/v1/aml-checks/history", "{\"page\":1,\"per_page\":10}", "{}",
                        c -> c.getAmlHistory()),
                wire("getAmlHistory with filter", "/v1/aml-checks/history",
                        "{\"page\":2,\"per_page\":5,\"status\":\"completed\"}", "{}",
                        c -> c.getAmlHistory(AmlHistoryRequest.of(2, 5, AmlStatus.COMPLETED))),
                wire("getSubscriptions", "/v1/subscriptions", "{}", "{}", c -> c.getSubscriptions()),
                wire("startSubscription all options", "/v1/subscription/start",
                        "{\"subscription_id\":\"unlimited_energy\",\"external_id\":\"sub-1\",\"params\":{\"address\":\"TAddress\",\"duration\":30,\"transactions_limit\":100,\"activate_address\":true}}", "{}",
                        c -> c.startSubscription(StartSubscriptionRequest.builder("unlimited_energy", "TAddress")
                                .durationDays(30).transactionsLimit(100).externalId("sub-1").activateAddress(true).build())),
                wire("startSubscription sends zero limits", "/v1/subscription/start",
                        "{\"subscription_id\":\"unlimited_energy\",\"params\":{\"address\":\"TAddress\",\"duration\":0,\"transactions_limit\":0}}", "{}",
                        c -> c.startSubscription(StartSubscriptionRequest.of("unlimited_energy", "TAddress"))),
                wire("startSubscription keeps external id 0", "/v1/subscription/start",
                        "{\"subscription_id\":\"unlimited_energy\",\"external_id\":\"0\",\"params\":{\"address\":\"TAddress\",\"duration\":1,\"transactions_limit\":0}}", "{}",
                        c -> c.startSubscription(StartSubscriptionRequest.builder("unlimited_energy", "TAddress")
                                .durationDays(1).externalId("0").build())),
                wire("checkSubscription by id", "/v1/subscription/check", "{\"id\":\"sub-id\"}", "{}",
                        c -> c.checkSubscription(SubscriptionRequest.byId("sub-id"))),
                wire("checkSubscription by external id", "/v1/subscription/check", "{\"external_id\":\"sub-1\"}", "{}",
                        c -> c.checkSubscription(SubscriptionRequest.byExternalId("sub-1"))),
                wire("stopSubscription with both ids", "/v1/subscription/stop",
                        "{\"id\":\"sub-id\",\"external_id\":\"sub-1\"}", "{}",
                        c -> c.stopSubscription(new SubscriptionRequest(Optional.of("sub-id"), Optional.of("sub-1")))),
                wire("getSubscriptionHistory defaults", "/v1/subscriptions/history", "{\"page\":1,\"per_page\":10}", "{}",
                        c -> c.getSubscriptionHistory()),
                wire("getSubscriptionHistory with filter", "/v1/subscriptions/history",
                        "{\"page\":2,\"per_page\":50,\"status\":\"active\"}", "{}",
                        c -> c.getSubscriptionHistory(SubscriptionHistoryRequest.of(2, 50, SubscriptionStatus.ACTIVE))));
    }

    private static Arguments wire(String name, String path, String body, String result, Consumer<TronzapClient> call) {
        return Arguments.of(name, path, body, result, call);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void sendsExactRequest(String name, String path, String expectedBody, String result, Consumer<TronzapClient> call)
            throws Exception {
        try (TestServer server = TestServer.start().replyOk(result)) {
            call.accept(server.client());

            TestServer.Received request = server.onlyRequest();
            assertEquals("POST", request.method());
            assertEquals(path, request.path());
            assertEquals(JSON.readTree(expectedBody), JSON.readTree(request.body()), "body of " + name);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void signsTheBytesTheServerReceived(String name, String path, String expectedBody, String result, Consumer<TronzapClient> call)
            throws Exception {
        try (TestServer server = TestServer.start().replyOk(result)) {
            call.accept(server.client());

            TestServer.Received request = server.onlyRequest();
            assertEquals(sha256Hex(request.body(), TestServer.SECRET), request.header("X-Signature"));
            assertEquals("Bearer " + TestServer.TOKEN, request.header("Authorization"));
            assertEquals("application/json", request.header("Content-Type"));
            assertEquals("application/json", request.header("Accept"));
        }
    }

    @Test
    void emptyParametersAreSentAsJsonObject() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            server.client().getBalance();

            assertEquals("{}", server.onlyRequest().bodyText());
        }
    }

    @Test
    void signatureChangesWithTheSecret() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            server.clientBuilder().apiSecret("another-secret").build().getBalance();

            TestServer.Received request = server.onlyRequest();
            assertEquals(sha256Hex(request.body(), "another-secret"), request.header("X-Signature"));
        }
    }

    @Test
    void sendsDefaultUserAgent() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            server.client().getBalance();

            assertEquals("tronzap-sdk-java/" + TronzapClient.VERSION, server.onlyRequest().header("User-Agent"));
        }
    }

    @Test
    void sendsCustomUserAgent() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            server.clientBuilder().userAgent("my-app/2.0").build().getBalance();

            assertEquals("my-app/2.0", server.onlyRequest().header("User-Agent"));
        }
    }

    @Test
    void nonAsciiValuesAreSignedAsUtf8() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            server.client().createEnergyTransaction(EnergyTransactionRequest.builder("TAddress", 65000)
                    .externalId("pedido-año-订单-😀").build());

            TestServer.Received request = server.onlyRequest();
            assertEquals("pedido-año-订单-😀", JSON.readTree(request.body()).path("external_id").asText());
            assertEquals(sha256Hex(request.body(), TestServer.SECRET), request.header("X-Signature"));
        }
    }

    @Test
    void appendsEndpointToBaseUrlPath() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            server.clientBuilder().baseUrl(server.url() + "/proxy/").build().getBalance();

            assertEquals("/proxy/v1/balance", server.onlyRequest().path());
        }
    }

    static String sha256Hex(byte[] body, String secret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(body);
        digest.update(secret.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest.digest());
    }
}
