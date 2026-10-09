package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tronzap.sdk.exception.InvalidResponseException;
import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlRiskLevel;
import com.tronzap.sdk.model.AmlStatus;
import com.tronzap.sdk.model.AmlType;
import com.tronzap.sdk.model.Service;
import com.tronzap.sdk.model.SubscriptionParams;
import com.tronzap.sdk.model.SubscriptionStatus;
import com.tronzap.sdk.model.TransactionStatus;
import com.tronzap.sdk.request.CheckTransactionRequest;
import com.tronzap.sdk.request.EnergyTransactionRequest;
import com.tronzap.sdk.request.StartSubscriptionRequest;
import com.tronzap.sdk.request.SubscriptionRequest;
import com.tronzap.sdk.response.AddressInfo;
import com.tronzap.sdk.response.AmlCheck;
import com.tronzap.sdk.response.AmlHistory;
import com.tronzap.sdk.response.AmlService;
import com.tronzap.sdk.response.Balance;
import com.tronzap.sdk.response.Calculation;
import com.tronzap.sdk.response.DirectRechargeInfo;
import com.tronzap.sdk.response.EnergyEstimate;
import com.tronzap.sdk.response.Services;
import com.tronzap.sdk.response.Subscription;
import com.tronzap.sdk.response.SubscriptionHistory;
import com.tronzap.sdk.response.SubscriptionPlan;
import com.tronzap.sdk.response.Transaction;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ResponseMappingTest {

    private static <T> T respond(String result, Function<TronzapClient, T> call) throws IOException {
        try (TestServer server = TestServer.start().replyOk(result)) {
            return call.apply(server.client());
        }
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    @Test
    void services() throws Exception {
        Services services = respond("""
                {
                  "energy": [
                    {"duration":1,"min_amount":50000,"max_amount":131000,
                     "price":0.052300000,"price_32k":1.67,"price_65k":3.4,"price_131k":6.85}
                  ],
                  "bandwidth": [{"duration":1,"min_amount":1000,"max_amount":50000,"price":1}],
                  "activate_address": {"price":1.4}
                }""", TronzapClient::getServices);

        assertEquals(1, services.energy().size());
        var tier = services.energy().get(0);
        assertEquals(50000, tier.minAmount());
        assertEquals(131000, tier.maxAmount());
        assertEquals(new BigDecimal("0.052300000"), tier.price(), "scale is kept as sent");
        assertDecimal("6.85", tier.price131k());
        assertDecimal("1", services.bandwidth().get(0).price());
        assertDecimal("1.4", services.activateAddress().orElseThrow().price());
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedEnergyRateBoundsMirrorAmounts() throws Exception {
        Services services = respond("""
                {"energy": [
                  {"duration":1,"min_amount":50000,"max_amount":131000,
                   "price":0.03,"price_32k":0.96,"price_65k":1.95,"price_131k":3.93},
                  {"duration":24,"min_amount":32000,"max_amount":65000,"min_energy":1,"max_energy":2,
                   "price":0.03,"price_32k":0.96,"price_65k":1.95,"price_131k":3.93}
                ]}""", TronzapClient::getServices);

        var withoutDeprecated = services.energy().get(0);
        assertEquals(50000, withoutDeprecated.minEnergy());
        assertEquals(131000, withoutDeprecated.maxEnergy());
        var withDiffering = services.energy().get(1);
        assertEquals(32000, withDiffering.minEnergy());
        assertEquals(65000, withDiffering.maxEnergy());
    }

    @Test
    void servicesWithoutOptionalSections() throws Exception {
        Services services = respond("{}", TronzapClient::getServices);

        assertTrue(services.energy().isEmpty());
        assertTrue(services.bandwidth().isEmpty());
        assertTrue(services.activateAddress().isEmpty());
    }

    @Test
    void balance() throws Exception {
        Balance balance = respond("{\"balance\":100.50,\"address\":\"TDeposit\"}", TronzapClient::getBalance);

        assertDecimal("100.5", balance.balance());
        assertEquals("TDeposit", balance.address());
    }

    @Test
    void balanceAsString() throws Exception {
        Balance balance = respond("{\"balance\":\"100.50\",\"address\":\"TDeposit\"}", TronzapClient::getBalance);

        assertEquals(new BigDecimal("100.50"), balance.balance());
    }

    @Test
    void addressInfo() throws Exception {
        AddressInfo info = respond("""
                {"resources":{"energy":131000,"bandwidth":600},"balances":{"TRX":10,"USDT":"2.5"}}""",
                client -> client.getAddressInfo("TAddress"));

        assertEquals(131000, info.resources().energy());
        assertEquals(600, info.resources().bandwidth());
        assertDecimal("10", info.balances().get("TRX"));
        assertDecimal("2.5", info.balances().get("USDT"));
    }

    @Test
    void addressInfoWithPhpEmptyArrays() throws Exception {
        AddressInfo info = respond("{\"resources\":[],\"balances\":[]}", client -> client.getAddressInfo("TAddress"));

        assertEquals(0, info.resources().energy());
        assertTrue(info.balances().isEmpty());
    }

    @Test
    void energyEstimate() throws Exception {
        EnergyEstimate estimate = respond("""
                {"amount":64400,"duration":1,"price":3.66,"activation_fee":0,"total":3.66,
                 "from_address":"TFrom","to_address":"TTo","contract_address":"TContract"}""",
                client -> client.estimateEnergy(com.tronzap.sdk.request.EstimateEnergyRequest.of("TFrom", "TTo")));

        assertEquals(64400, estimate.amount());
        assertDecimal("3.66", estimate.total());
        assertDecimal("0", estimate.activationFee());
        assertEquals("TContract", estimate.contractAddress());
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedEstimateEnergyMirrorsAmount() throws Exception {
        String withoutDeprecated = """
                {"amount":65000,"duration":1,"price":1.95,"activation_fee":0,"total":1.95,
                 "from_address":"TFrom","to_address":"TTo","contract_address":"TContract"}""";
        String withDiffering = """
                {"amount":65000,"energy":1,"duration":1,"price":1.95,"activation_fee":0,"total":1.95,
                 "from_address":"TFrom","to_address":"TTo","contract_address":"TContract"}""";

        for (String result : List.of(withoutDeprecated, withDiffering)) {
            EnergyEstimate estimate = respond(result,
                    client -> client.estimateEnergy(com.tronzap.sdk.request.EstimateEnergyRequest.of("TFrom", "TTo")));
            assertEquals(65000, estimate.energy(), result);
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedCalculationEnergyMirrorsAmount() throws Exception {
        String withoutDeprecated = """
                {"address":"TAddress","type":"energy","amount":65000,"duration":1,"price":1.95,"activation_fee":0,"total":1.95}""";
        String withDiffering = """
                {"address":"TAddress","type":"energy","amount":65000,"energy":1,"duration":1,"price":1.95,"activation_fee":0,"total":1.95}""";

        for (String result : List.of(withoutDeprecated, withDiffering)) {
            Calculation calculation = respond(result,
                    client -> client.calculate(com.tronzap.sdk.request.CalculateRequest.of("TAddress", 65000)));
            assertEquals(65000, calculation.energy(), result);
        }
    }

    @Test
    void calculation() throws Exception {
        Calculation calculation = respond("""
                {"address":"TAddress","type":"energy","amount":65000,
                 "duration":1,"price":1.67,"activation_fee":0,"total":1.67}""",
                client -> client.calculate(com.tronzap.sdk.request.CalculateRequest.of("TAddress", 65000)));

        assertEquals(Service.ENERGY, calculation.type());
        assertEquals(65000, calculation.amount());
        assertDecimal("1.67", calculation.total());
    }

    @Test
    void createdTransactionHasNumericAmount() throws Exception {
        Transaction tx = respond("""
                {"id":"tx-1","external_id":"ext-1","service":"energy",
                 "params":{"address":"TAddress","amounts":{"energy":65000},"duration":1,"activate_address":true},
                 "status":"new","amount":3.4,"created_at":"2026-08-07T10:42:12+00:00","hash":""}""",
                client -> client.createEnergyTransaction(EnergyTransactionRequest.of("TAddress", 65000)));

        assertEquals("tx-1", tx.id());
        assertEquals("ext-1", tx.externalId().orElseThrow());
        assertEquals(Service.ENERGY, tx.service());
        assertEquals(TransactionStatus.NEW, tx.status());
        assertDecimal("3.4", tx.amount());
        assertEquals(65000, tx.params().amounts().energy());
        assertEquals(1, tx.params().duration());
        assertTrue(tx.params().activateAddress());
        assertEquals(OffsetDateTime.of(2026, 8, 7, 10, 42, 12, 0, ZoneOffset.UTC), tx.createdAt().orElseThrow().value().orElseThrow());
        assertEquals("2026-08-07T10:42:12+00:00", tx.createdAt().orElseThrow().raw());
        assertTrue(tx.hash().isEmpty(), "an empty hash means not settled yet");
    }

    @Test
    void checkedTransactionHasStringAmountAndLegacyParams() throws Exception {
        Transaction tx = respond("""
                {"id":"tx-1","external_id":null,"service":"energy",
                 "params":{"address":"TAddress","energy_amount":65000,"duration":1},
                 "status":"success","amount":"3.40","created_at":"2026-08-07 10:42:12","hash":"abc"}""",
                client -> client.checkTransaction(CheckTransactionRequest.byId("tx-1")));

        assertEquals(new BigDecimal("3.40"), tx.amount());
        assertTrue(tx.externalId().isEmpty());
        assertEquals(65000, tx.params().amounts().energy());
        assertEquals("abc", tx.hash().orElseThrow());
        assertEquals(TransactionStatus.SUCCESS, tx.status());
        assertEquals(OffsetDateTime.of(2026, 8, 7, 10, 42, 12, 0, ZoneOffset.UTC), tx.createdAt().orElseThrow().value().orElseThrow());
    }

    @Test
    void legacySingleAmountBelongsToTheService() throws Exception {
        Transaction bandwidth = respond("""
                {"id":"tx-2","service":"bandwidth","params":{"address":"TAddress","amount":345},"status":"pending"}""",
                client -> client.checkTransaction(CheckTransactionRequest.byId("tx-2")));
        Transaction energy = respond("""
                {"id":"tx-3","service":"energy","params":{"address":"TAddress","amount":65000},"status":"pending"}""",
                client -> client.checkTransaction(CheckTransactionRequest.byId("tx-3")));

        assertEquals(345, bandwidth.params().amounts().bandwidth());
        assertEquals(0, bandwidth.params().amounts().energy());
        assertEquals(65000, energy.params().amounts().energy());
        assertEquals(TransactionStatus.PENDING, energy.status());
    }

    @Test
    void resourceBundleAmounts() throws Exception {
        Transaction tx = respond("""
                {"id":"tx-4","service":"resource_bundle",
                 "params":{"address":"TAddress","amounts":{"energy":65000,"bandwidth":345},"duration":1},
                 "status":"failed","amount":4.1}""",
                client -> client.checkTransaction(CheckTransactionRequest.byId("tx-4")));

        assertEquals(Service.RESOURCE_BUNDLE, tx.service());
        assertEquals(65000, tx.params().amounts().energy());
        assertEquals(345, tx.params().amounts().bandwidth());
        assertEquals(TransactionStatus.FAILED, tx.status());
        assertTrue(tx.createdAt().isEmpty());
    }

    @Test
    void unknownValuesAndFieldsDoNotBreakDecoding() throws Exception {
        Transaction tx = respond("""
                {"id":"tx-5","service":"quantum_energy","status":"refunded","amount":1,
                 "params":{"address":"TAddress","future_field":{"nested":[1,2,3]}},
                 "brand_new_field":"value","created_at":"next tuesday"}""",
                client -> client.checkTransaction(CheckTransactionRequest.byId("tx-5")));

        assertEquals(Service.UNKNOWN, tx.service());
        assertEquals(TransactionStatus.UNKNOWN, tx.status());
        assertEquals("next tuesday", tx.createdAt().orElseThrow().raw());
        assertTrue(tx.createdAt().orElseThrow().value().isEmpty());
    }

    @Test
    void directRechargeInfo() throws Exception {
        DirectRechargeInfo info = respond("""
                {"address":"TPublic",
                 "rates":[{"duration":1,"min_energy":50000,"max_energy":131000,"price":0.0523,"price_32k":1.67,"price_65k":3.4,"price_131k":6.85}]}""",
                TronzapClient::getDirectRechargeInfo);

        assertEquals("TPublic", info.address());
        assertEquals(1, info.rates().size());
        assertDecimal("3.4", info.rates().get(0).price65k());
    }

    @Test
    void amlServices() throws Exception {
        List<AmlService> services = respond("""
                [{"id":"01K834","type":"address","price":2.5},{"id":"01K835","type":"hash","price":"3.75"}]""",
                TronzapClient::getAmlServices);

        assertEquals(2, services.size());
        assertEquals(AmlType.HASH, services.get(1).type());
        assertDecimal("3.75", services.get(1).price());
    }

    @Test
    void emptyAmlServicesAsPhpObject() throws Exception {
        assertTrue(respond("{}", TronzapClient::getAmlServices).isEmpty());
    }

    @Test
    void pendingAmlCheckHasNoScore() throws Exception {
        AmlCheck check = respond("""
                {"id":"aml-1","type":"hash","address":"bc1address","hash":"E3F2","direction":"withdrawal",
                 "network":"BTC","status":"processing","risk_score":null,"risk_level":null,
                 "blacklist":false,"risk_factors":[],"checked_at":"2026-08-07T10:42:12Z"}""",
                client -> client.checkAmlStatus("aml-1"));

        assertEquals(AmlStatus.PROCESSING, check.status());
        assertEquals(AmlType.HASH, check.type());
        assertEquals(AmlDirection.WITHDRAWAL, check.direction().orElseThrow());
        assertEquals("E3F2", check.hash().orElseThrow());
        assertTrue(check.riskScore().isEmpty());
        assertTrue(check.riskLevel().isEmpty());
        assertTrue(check.riskFactors().isEmpty());
        assertFalse(check.blacklist());
    }

    @Test
    void completedAmlCheck() throws Exception {
        AmlCheck check = respond("""
                {"id":"aml-1","type":"address","address":"0x6Dc1","hash":null,"direction":null,
                 "network":"ETH","status":"completed","risk_score":"12.5","risk_level":"medium",
                 "blacklist":false,
                 "risk_factors":[{"name":"exchange","label":"Exchange","group":"low","score":0.203}],
                 "checked_at":"2026-08-07T10:42:12Z"}""",
                client -> client.checkAmlStatus("aml-1"));

        assertDecimal("12.5", check.riskScore().orElseThrow());
        assertEquals(AmlRiskLevel.MEDIUM, check.riskLevel().orElseThrow());
        assertTrue(check.hash().isEmpty());
        assertTrue(check.direction().isEmpty());
        assertEquals(1, check.riskFactors().size());
        assertDecimal("0.203", check.riskFactors().get(0).score());
        assertEquals("Exchange", check.riskFactors().get(0).label());
        assertTrue(check.checkedAt().orElseThrow().value().isPresent());
    }

    @Test
    void zeroRiskScoreIsDifferentFromNoScore() throws Exception {
        AmlCheck check = respond("{\"id\":\"aml-2\",\"status\":\"completed\",\"risk_score\":\"0\",\"risk_level\":\"low\"}",
                client -> client.checkAmlStatus("aml-2"));

        assertDecimal("0", check.riskScore().orElseThrow());
        assertEquals(AmlRiskLevel.LOW, check.riskLevel().orElseThrow());
    }

    @Test
    void amlHistory() throws Exception {
        AmlHistory history = respond("""
                {"page":1,"per_page":2,"total":5,
                 "items":[{"id":"aml-1","type":"hash","status":"completed","risk_score":"64.3","risk_level":"high","blacklist":true,"risk_factors":[]}]}""",
                TronzapClient::getAmlHistory);

        assertEquals(5, history.total());
        assertEquals(2, history.perPage());
        assertEquals(1, history.items().size());
        assertTrue(history.items().get(0).blacklist());
        assertEquals(AmlRiskLevel.HIGH, history.items().get(0).riskLevel().orElseThrow());
        assertDecimal("64.3", history.items().get(0).riskScore().orElseThrow());
    }

    @Test
    void lenientScalarEncodings() throws Exception {
        AmlHistory history = respond("""
                {"page":"1","per_page":10.0,"total":"","items":[{"id":7,"blacklist":"1"},{"id":"a","blacklist":0},{"id":"b","blacklist":"false"}]}""",
                TronzapClient::getAmlHistory);

        assertEquals(1, history.page());
        assertEquals(10, history.perPage());
        assertEquals(0, history.total());
        assertEquals("7", history.items().get(0).id());
        assertTrue(history.items().get(0).blacklist());
        assertFalse(history.items().get(1).blacklist());
        assertFalse(history.items().get(2).blacklist());
    }

    @Test
    void collectionsAreNeverNullAndImmutable() throws Exception {
        AmlHistory history = respond("{\"items\":null}", TronzapClient::getAmlHistory);

        assertTrue(history.items().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> history.items().add(null));
    }

    private static final String STARTED_SUBSCRIPTION = """
            {"id":"01m4e1z3q0r7x225zc6p63m5ey","subscription_id":"unlimited_energy",
             "created_at":"2026-10-08T15:26:32+00:00","expire_at":"2026-11-07T15:26:32+00:00",
             "address":"TAddress","status":"active","external_id":"sub-1",
             "params":{"address":"TAddress","duration":30,"transactions_limit":0,"activate_address":false}}""";

    @Test
    void subscriptionPlansKeepTheApiOrder() throws Exception {
        List<SubscriptionPlan> plans = respond("""
                {"unlimited_energy":{"id":8,"name":"Unlimited Energy","activation_fee":0,"initial_price":8,
                                     "price":2.8,"transactions_limit":0,"duration_days":0},
                 "energy_pack_100":{"id":2,"name":"Energy Pack","activation_fee":"2.0","initial_price":"12.5",
                                    "price":"1.5","transactions_limit":10,"duration_days":5}}""",
                TronzapClient::getSubscriptions);

        assertEquals(2, plans.size());
        SubscriptionPlan unlimited = plans.get(0);
        assertEquals("unlimited_energy", unlimited.subscriptionId());
        assertEquals(8, unlimited.id());
        assertEquals("Unlimited Energy", unlimited.name());
        assertDecimal("0", unlimited.activationFee());
        assertDecimal("8", unlimited.initialPrice());
        assertDecimal("2.8", unlimited.price());
        assertEquals(0, unlimited.transactionsLimit());
        assertEquals(0, unlimited.durationDays());
        SubscriptionPlan pack = plans.get(1);
        assertEquals("energy_pack_100", pack.subscriptionId());
        assertEquals(2, pack.id());
        assertDecimal("2.0", pack.activationFee());
        assertDecimal("1.5", pack.price());
        assertEquals(10, pack.transactionsLimit());
        assertEquals(5, pack.durationDays());
        assertThrows(UnsupportedOperationException.class, () -> plans.add(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "[]"})
    void noSubscriptionPlans(String result) throws Exception {
        assertTrue(respond(result, TronzapClient::getSubscriptions).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"plans\"", "42", "true", "{\"unlimited_energy\":\"cheap\"}"})
    void subscriptionPlansOfTheWrongShape(String result) throws Exception {
        try (TestServer server = TestServer.start().replyOk(result)) {
            assertThrows(InvalidResponseException.class, () -> server.client().getSubscriptions());
        }
    }

    @Test
    void startedSubscription() throws Exception {
        Subscription sub = respond(STARTED_SUBSCRIPTION,
                client -> client.startSubscription(StartSubscriptionRequest.of("unlimited_energy", "TAddress")));

        assertEquals("01m4e1z3q0r7x225zc6p63m5ey", sub.id());
        assertEquals("unlimited_energy", sub.subscriptionId());
        assertEquals("sub-1", sub.externalId().orElseThrow());
        assertEquals("TAddress", sub.address().orElseThrow());
        assertEquals(SubscriptionStatus.ACTIVE, sub.status());
        assertEquals(new SubscriptionParams("TAddress", 30, 0, false), sub.params().orElseThrow());
        assertEquals(OffsetDateTime.of(2026, 10, 8, 15, 26, 32, 0, ZoneOffset.UTC), sub.createdAt().orElseThrow().value().orElseThrow());
        assertEquals("2026-11-07T15:26:32+00:00", sub.expireAt().orElseThrow().raw());
        assertTrue(sub.startedAt().isEmpty());
        assertTrue(sub.stoppedAt().isEmpty());
        assertEquals(0, sub.transactionsUsed());
        assertDecimal("0", sub.totalPrice());
    }

    @Test
    void checkedSubscriptionMatchesStarted() throws Exception {
        Subscription started = respond(STARTED_SUBSCRIPTION,
                client -> client.startSubscription(StartSubscriptionRequest.of("unlimited_energy", "TAddress")));
        Subscription checked = respond(STARTED_SUBSCRIPTION,
                client -> client.checkSubscription(SubscriptionRequest.byId("01m4e1z3q0r7x225zc6p63m5ey")));

        assertEquals(started, checked);
    }

    @Test
    void stoppedSubscriptionWithoutAddressOrExpiry() throws Exception {
        Subscription sub = respond("""
                {"id":"01m4e1z3q0r7x225zc6p63m5ey","subscription_id":"unlimited_energy",
                 "created_at":"2026-10-08T15:26:32+00:00","stopped_at":"2026-10-08T15:28:44+00:00",
                 "status":"stopped","external_id":"sub-1",
                 "params":{"address":"TAddress","duration":30,"transactions_limit":0,"activate_address":false}}""",
                client -> client.stopSubscription(SubscriptionRequest.byExternalId("sub-1")));

        assertEquals(SubscriptionStatus.STOPPED, sub.status());
        assertTrue(sub.address().isEmpty());
        assertTrue(sub.expireAt().isEmpty());
        assertEquals("2026-10-08T15:28:44+00:00", sub.stoppedAt().orElseThrow().raw());
        assertEquals("TAddress", sub.params().orElseThrow().address());
        assertEquals(30, sub.params().orElseThrow().durationDays());
    }

    @Test
    void subscriptionHistory() throws Exception {
        SubscriptionHistory history = respond("""
                {"page":1,"per_page":10,"total":1,"items":[
                 {"id":"01m4e1z3q0r7x225zc6p63m5ey","status":"active","subscription_id":"unlimited_energy",
                  "address":"TAddress","transactions_limit":0,"transactions_used":4,"energy_used":262000,
                  "total_price":13.6,"started_at":"2026-10-08T15:26:33+00:00","renewed_at":"2026-10-08T15:27:35+00:00",
                  "stopped_at":null,"expire_at":"2026-11-07T15:26:32+00:00","created_at":"2026-10-08T15:26:32+00:00"}]}""",
                TronzapClient::getSubscriptionHistory);

        assertEquals(1, history.page());
        assertEquals(10, history.perPage());
        assertEquals(1, history.total());
        Subscription sub = history.items().get(0);
        assertEquals(SubscriptionStatus.ACTIVE, sub.status());
        assertEquals("TAddress", sub.address().orElseThrow());
        assertTrue(sub.params().isEmpty());
        assertTrue(sub.externalId().isEmpty());
        assertEquals(0, sub.transactionsLimit());
        assertEquals(4, sub.transactionsUsed());
        assertEquals(262000, sub.energyUsed());
        assertDecimal("13.6", sub.totalPrice());
        assertEquals("2026-10-08T15:26:33+00:00", sub.startedAt().orElseThrow().raw());
        assertEquals("2026-10-08T15:27:35+00:00", sub.renewedAt().orElseThrow().raw());
        assertTrue(sub.stoppedAt().isEmpty());
        assertTrue(sub.expireAt().isPresent());
    }

    @Test
    void subscriptionTotalPriceAsStringAndUnknownStatus() throws Exception {
        SubscriptionHistory history = respond("""
                {"page":1,"per_page":10,"total":1,
                 "items":[{"id":"sub-2","status":"paused","total_price":"8.00","future_field":{"nested":[1]}}]}""",
                TronzapClient::getSubscriptionHistory);

        Subscription sub = history.items().get(0);
        assertEquals(new BigDecimal("8.00"), sub.totalPrice());
        assertEquals(SubscriptionStatus.UNKNOWN, sub.status());
    }
}
