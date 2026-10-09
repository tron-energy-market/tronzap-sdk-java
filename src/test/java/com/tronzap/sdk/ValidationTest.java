package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlStatus;
import com.tronzap.sdk.model.AmlType;
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
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidationTest {

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                invalid("estimate without sender", () -> EstimateEnergyRequest.of("", "TTo")),
                invalid("estimate without recipient", () -> EstimateEnergyRequest.of("TFrom", null)),
                invalid("estimate blank contract", () -> EstimateEnergyRequest.of("TFrom", "TTo", " ")),
                invalid("calculate without address", () -> CalculateRequest.of(" ", 65000)),
                invalid("calculate zero energy", () -> CalculateRequest.of("TAddress", 0)),
                invalid("calculate zero duration", () -> new CalculateRequest("TAddress", 65000, 0)),
                invalid("energy negative amount", () -> EnergyTransactionRequest.of("TAddress", -1)),
                invalid("energy zero duration", () -> EnergyTransactionRequest.builder("TAddress", 65000).duration(0).build()),
                invalid("energy blank external id", () -> EnergyTransactionRequest.builder("TAddress", 65000).externalId("").build()),
                invalid("bandwidth without address", () -> BandwidthTransactionRequest.of(null, 345)),
                invalid("bandwidth zero amount", () -> BandwidthTransactionRequest.of("TAddress", 0)),
                invalid("bandwidth blank external id", () -> BandwidthTransactionRequest.of("TAddress", 345, "  ")),
                invalid("bundle zero bandwidth", () -> ResourceBundleTransactionRequest.of("TAddress", 65000, 0)),
                invalid("bundle zero energy", () -> ResourceBundleTransactionRequest.of("TAddress", 0, 345)),
                invalid("bundle negative duration", () -> ResourceBundleTransactionRequest.builder("TAddress", 65000, 345).duration(-1).build()),
                invalid("activation without address", () -> AddressActivationRequest.of("")),
                invalid("activation blank external id", () -> AddressActivationRequest.of("TAddress", "")),
                invalid("check without any id", () -> new CheckTransactionRequest(Optional.empty(), Optional.empty())),
                invalid("check blank id", () -> CheckTransactionRequest.byId(" ")),
                invalid("check blank external id", () -> CheckTransactionRequest.byExternalId("")),
                invalid("aml without network", () -> AmlCheckRequest.forAddress("", "TAddress")),
                invalid("aml without address", () -> AmlCheckRequest.forAddress("TRX", null)),
                invalid("aml hash without hash", () -> AmlCheckRequest.forHash("BTC", "bc1address", null)),
                invalid("aml unknown type", () -> new AmlCheckRequest(AmlType.UNKNOWN, "TRX", "TAddress", Optional.empty(), Optional.empty())),
                invalid("aml unknown direction", () -> AmlCheckRequest.forHash("BTC", "bc1address", "E3F2", AmlDirection.UNKNOWN)),
                invalid("history zero page", () -> AmlHistoryRequest.of(0, 10)),
                invalid("history zero page size", () -> AmlHistoryRequest.of(1, 0)),
                invalid("history unknown status", () -> AmlHistoryRequest.of(1, 10, AmlStatus.UNKNOWN)),
                invalid("subscription without plan", () -> StartSubscriptionRequest.of("", "TAddress")),
                invalid("subscription null plan", () -> StartSubscriptionRequest.of(null, "TAddress")),
                invalid("subscription without address", () -> StartSubscriptionRequest.of("unlimited_energy", " ")),
                invalid("subscription negative days", () -> StartSubscriptionRequest.builder("unlimited_energy", "TAddress").durationDays(-1).build()),
                invalid("subscription negative limit", () -> StartSubscriptionRequest.builder("unlimited_energy", "TAddress").transactionsLimit(-1).build()),
                invalid("subscription blank external id", () -> StartSubscriptionRequest.builder("unlimited_energy", "TAddress").externalId("").build()),
                invalid("subscription lookup without any id", () -> new SubscriptionRequest(Optional.empty(), Optional.empty())),
                invalid("subscription lookup blank id", () -> SubscriptionRequest.byId(" ")),
                invalid("subscription lookup blank external id", () -> SubscriptionRequest.byExternalId(null)),
                invalid("subscription history zero page", () -> SubscriptionHistoryRequest.of(0, 10)),
                invalid("subscription history zero page size", () -> SubscriptionHistoryRequest.of(1, 0)),
                invalid("subscription history unknown status", () -> SubscriptionHistoryRequest.of(1, 10, SubscriptionStatus.UNKNOWN)));
    }

    private static Arguments invalid(String name, Executable construction) {
        return Arguments.of(name, construction);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequests")
    void rejectsInvalidRequest(String name, Executable construction) {
        assertThrows(IllegalArgumentException.class, construction);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void invalidArgumentsAreNeverSent(String blank) throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            TronzapClient client = server.client();

            assertThrows(IllegalArgumentException.class, () -> client.getAddressInfo(blank));
            assertThrows(IllegalArgumentException.class, () -> client.checkAmlStatus(blank));
            assertThrows(IllegalArgumentException.class,
                    () -> client.startSubscription(StartSubscriptionRequest.of(blank, "TAddress")));
            assertThrows(IllegalArgumentException.class,
                    () -> client.startSubscription(StartSubscriptionRequest.of("unlimited_energy", blank)));
            assertThrows(IllegalArgumentException.class, () -> client.checkSubscription(SubscriptionRequest.byId(blank)));
            assertThrows(IllegalArgumentException.class, () -> client.stopSubscription(SubscriptionRequest.byExternalId(blank)));

            assertTrue(server.requests().isEmpty());
        }
    }

    @Test
    void nullRequestsAreRejectedBeforeSending() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{}")) {
            TronzapClient client = server.client();

            assertThrows(NullPointerException.class, () -> client.estimateEnergy(null));
            assertThrows(NullPointerException.class, () -> client.calculate(null));
            assertThrows(NullPointerException.class, () -> client.createEnergyTransaction(null));
            assertThrows(NullPointerException.class, () -> client.createBandwidthTransaction(null));
            assertThrows(NullPointerException.class, () -> client.createResourceBundleTransaction(null));
            assertThrows(NullPointerException.class, () -> client.createAddressActivationTransaction(null));
            assertThrows(NullPointerException.class, () -> client.checkTransaction(null));
            assertThrows(NullPointerException.class, () -> client.createAmlCheck(null));
            assertThrows(NullPointerException.class, () -> client.getAmlHistory(null));
            assertThrows(NullPointerException.class, () -> client.startSubscription(null));
            assertThrows(NullPointerException.class, () -> client.checkSubscription(null));
            assertThrows(NullPointerException.class, () -> client.stopSubscription(null));
            assertThrows(NullPointerException.class, () -> client.getSubscriptionHistory(null));

            assertTrue(server.requests().isEmpty());
        }
    }
}
