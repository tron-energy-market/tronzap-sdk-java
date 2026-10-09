package com.tronzap.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.tronzap.sdk.model.ActivateAddressRate;
import com.tronzap.sdk.model.AmlDirection;
import com.tronzap.sdk.model.AmlRiskFactor;
import com.tronzap.sdk.model.AmlRiskLevel;
import com.tronzap.sdk.model.AmlStatus;
import com.tronzap.sdk.model.AmlType;
import com.tronzap.sdk.model.Amounts;
import com.tronzap.sdk.model.BandwidthRate;
import com.tronzap.sdk.model.DirectRechargeRate;
import com.tronzap.sdk.model.EnergyRate;
import com.tronzap.sdk.model.Resources;
import com.tronzap.sdk.model.Service;
import com.tronzap.sdk.model.SubscriptionParams;
import com.tronzap.sdk.model.SubscriptionStatus;
import com.tronzap.sdk.model.Timestamp;
import com.tronzap.sdk.model.TransactionParams;
import com.tronzap.sdk.model.TransactionStatus;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

final class ResultMapper {

    private static final JsonNode EMPTY_OBJECT = JsonNodeFactory.instance.objectNode();

    private ResultMapper() {
    }

    static Balance balance(JsonNode node) {
        JsonNode o = object(node, "result");
        return new Balance(decimal(o, "balance"), text(o, "address"));
    }

    static Services services(JsonNode node) {
        JsonNode o = object(node, "result");
        JsonNode activation = o.get("activate_address");
        Optional<ActivateAddressRate> activateAddress = isAbsent(activation)
                ? Optional.empty()
                : Optional.of(new ActivateAddressRate(decimal(object(activation, "activate_address"), "price")));
        return new Services(
                list(o, "energy", ResultMapper::energyRate),
                list(o, "bandwidth", ResultMapper::bandwidthRate),
                activateAddress);
    }

    static AddressInfo addressInfo(JsonNode node) {
        JsonNode o = object(node, "result");
        JsonNode resources = object(o.get("resources"), "resources");
        return new AddressInfo(
                new Resources(integer(resources, "energy"), integer(resources, "bandwidth")),
                decimalMap(o, "balances"));
    }

    static EnergyEstimate energyEstimate(JsonNode node) {
        JsonNode o = object(node, "result");
        long amount = integer(o, "amount");
        return new EnergyEstimate(
                amount,
                amount,
                int32(o, "duration"),
                decimal(o, "price"),
                decimal(o, "activation_fee"),
                decimal(o, "total"),
                text(o, "from_address"),
                text(o, "to_address"),
                text(o, "contract_address"));
    }

    static Calculation calculation(JsonNode node) {
        JsonNode o = object(node, "result");
        long amount = integer(o, "amount");
        return new Calculation(
                text(o, "address"),
                Service.fromValue(text(o, "type")),
                amount,
                amount,
                int32(o, "duration"),
                decimal(o, "price"),
                decimal(o, "activation_fee"),
                decimal(o, "total"));
    }

    static Transaction transaction(JsonNode node) {
        JsonNode o = object(node, "result");
        Service service = Service.fromValue(text(o, "service"));
        return new Transaction(
                text(o, "id"),
                optionalText(o, "external_id"),
                service,
                transactionParams(object(o.get("params"), "params"), service),
                TransactionStatus.fromValue(text(o, "status")),
                decimal(o, "amount"),
                timestamp(o, "created_at"),
                optionalText(o, "hash"));
    }

    static DirectRechargeInfo directRechargeInfo(JsonNode node) {
        JsonNode o = object(node, "result");
        return new DirectRechargeInfo(text(o, "address"), list(o, "rates", ResultMapper::directRechargeRate));
    }

    static List<AmlService> amlServices(JsonNode node) {
        return elements(node, "result", ResultMapper::amlService);
    }

    static AmlCheck amlCheck(JsonNode node) {
        JsonNode o = object(node, "AML check");
        return new AmlCheck(
                text(o, "id"),
                AmlType.fromValue(text(o, "type")),
                text(o, "address"),
                optionalText(o, "hash"),
                optionalText(o, "direction").map(AmlDirection::fromValue),
                text(o, "network"),
                AmlStatus.fromValue(text(o, "status")),
                optionalDecimal(o, "risk_score"),
                optionalText(o, "risk_level").map(AmlRiskLevel::fromValue),
                bool(o, "blacklist"),
                list(o, "risk_factors", ResultMapper::amlRiskFactor),
                timestamp(o, "checked_at"));
    }

    static AmlHistory amlHistory(JsonNode node) {
        JsonNode o = object(node, "result");
        return new AmlHistory(
                int32(o, "page"),
                int32(o, "per_page"),
                int32(o, "total"),
                list(o, "items", ResultMapper::amlCheck));
    }

    /** Plans arrive keyed by plan identifier; an empty set is encoded by PHP as {@code []}. */
    static List<SubscriptionPlan> subscriptionPlans(JsonNode node) {
        if (node != null && node.isArray()) {
            return elements(node, "result", item -> subscriptionPlan("", item));
        }
        if (node == null || !node.isObject()) {
            throw new MappingException("result is not an object");
        }
        List<SubscriptionPlan> plans = new ArrayList<>(node.size());
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            plans.add(subscriptionPlan(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(plans);
    }

    static Subscription subscription(JsonNode node) {
        JsonNode o = object(node, "subscription");
        JsonNode params = o.get("params");
        return new Subscription(
                text(o, "id"),
                text(o, "subscription_id"),
                optionalText(o, "external_id"),
                optionalText(o, "address"),
                SubscriptionStatus.fromValue(text(o, "status")),
                isAbsent(params) ? Optional.empty() : Optional.of(subscriptionParams(object(params, "params"))),
                integer(o, "transactions_limit"),
                integer(o, "transactions_used"),
                integer(o, "energy_used"),
                decimal(o, "total_price"),
                timestamp(o, "created_at"),
                timestamp(o, "started_at"),
                timestamp(o, "renewed_at"),
                timestamp(o, "stopped_at"),
                timestamp(o, "expire_at"));
    }

    static SubscriptionHistory subscriptionHistory(JsonNode node) {
        JsonNode o = object(node, "result");
        return new SubscriptionHistory(
                int32(o, "page"),
                int32(o, "per_page"),
                int32(o, "total"),
                list(o, "items", ResultMapper::subscription));
    }

    private static SubscriptionPlan subscriptionPlan(String key, JsonNode node) {
        JsonNode o = object(node, "subscription plan");
        return new SubscriptionPlan(
                key,
                integer(o, "id"),
                text(o, "name"),
                decimal(o, "activation_fee"),
                decimal(o, "initial_price"),
                decimal(o, "price"),
                integer(o, "transactions_limit"),
                int32(o, "duration_days"));
    }

    private static SubscriptionParams subscriptionParams(JsonNode o) {
        return new SubscriptionParams(
                text(o, "address"), int32(o, "duration"), integer(o, "transactions_limit"), bool(o, "activate_address"));
    }

    private static TransactionParams transactionParams(JsonNode o, Service service) {
        JsonNode amounts = object(o.get("amounts"), "amounts");
        long energy = integer(amounts, "energy");
        long bandwidth = integer(amounts, "bandwidth");
        if (energy == 0 && bandwidth == 0) {
            energy = integer(o, "energy_amount");
            long amount = integer(o, "amount");
            if (service == Service.BANDWIDTH) {
                bandwidth = amount;
            } else if (energy == 0) {
                energy = amount;
            }
        }
        return new TransactionParams(text(o, "address"), int32(o, "duration"), new Amounts(energy, bandwidth), bool(o, "activate_address"));
    }

    private static EnergyRate energyRate(JsonNode node) {
        JsonNode o = object(node, "energy rate");
        long minAmount = integer(o, "min_amount");
        long maxAmount = integer(o, "max_amount");
        return new EnergyRate(
                int32(o, "duration"),
                minAmount,
                maxAmount,
                minAmount,
                maxAmount,
                decimal(o, "price"),
                decimal(o, "price_32k"),
                decimal(o, "price_65k"),
                decimal(o, "price_131k"));
    }

    private static BandwidthRate bandwidthRate(JsonNode node) {
        JsonNode o = object(node, "bandwidth rate");
        return new BandwidthRate(int32(o, "duration"), integer(o, "min_amount"), integer(o, "max_amount"), decimal(o, "price"));
    }

    private static DirectRechargeRate directRechargeRate(JsonNode node) {
        JsonNode o = object(node, "direct recharge rate");
        return new DirectRechargeRate(
                int32(o, "duration"),
                integer(o, "min_energy"),
                integer(o, "max_energy"),
                decimal(o, "price"),
                decimal(o, "price_32k"),
                decimal(o, "price_65k"),
                decimal(o, "price_131k"));
    }

    private static AmlService amlService(JsonNode node) {
        JsonNode o = object(node, "AML service");
        return new AmlService(text(o, "id"), AmlType.fromValue(text(o, "type")), decimal(o, "price"));
    }

    private static AmlRiskFactor amlRiskFactor(JsonNode node) {
        JsonNode o = object(node, "risk factor");
        return new AmlRiskFactor(text(o, "name"), text(o, "label"), text(o, "group"), decimal(o, "score"));
    }

    private static boolean isAbsent(JsonNode node) {
        return node == null || node.isNull();
    }

    /** The API is backed by PHP, which encodes an empty associative array as {@code []}. */
    private static JsonNode object(JsonNode node, String name) {
        if (isAbsent(node) || (node.isArray() && node.isEmpty())) {
            return EMPTY_OBJECT;
        }
        if (!node.isObject()) {
            throw new MappingException(name + " is not an object");
        }
        return node;
    }

    private static <T> List<T> list(JsonNode o, String field, Function<JsonNode, T> element) {
        return elements(o.get(field), field, element);
    }

    private static <T> List<T> elements(JsonNode node, String name, Function<JsonNode, T> element) {
        if (isAbsent(node) || (node.isObject() && node.isEmpty())) {
            return List.of();
        }
        if (!node.isArray()) {
            throw new MappingException(name + " is not an array");
        }
        List<T> items = new ArrayList<>(node.size());
        for (JsonNode item : node) {
            items.add(element.apply(item));
        }
        return Collections.unmodifiableList(items);
    }

    private static Map<String, BigDecimal> decimalMap(JsonNode o, String field) {
        JsonNode node = object(o.get(field), field);
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            values.put(entry.getKey(), toDecimal(entry.getValue(), field + "." + entry.getKey()));
        }
        return values;
    }

    private static String text(JsonNode o, String field) {
        JsonNode node = o.get(field);
        if (isAbsent(node)) {
            return "";
        }
        if (node.isTextual()) {
            return node.textValue();
        }
        if (node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        throw new MappingException(field + " is not a string");
    }

    private static Optional<String> optionalText(JsonNode o, String field) {
        String value = text(o, field);
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    private static long integer(JsonNode o, String field) {
        JsonNode node = o.get(field);
        if (isAbsent(node)) {
            return 0;
        }
        try {
            if (node.isIntegralNumber() && node.canConvertToLong()) {
                return node.longValue();
            }
            if (node.isNumber()) {
                return node.decimalValue().longValueExact();
            }
            if (node.isTextual()) {
                String text = node.textValue().trim();
                return text.isEmpty() ? 0 : new BigDecimal(text).longValueExact();
            }
        } catch (ArithmeticException | NumberFormatException e) {
            throw new MappingException(field + " is not an integer: " + node, e);
        }
        throw new MappingException(field + " is not an integer: " + node);
    }

    private static int int32(JsonNode o, String field) {
        long value = integer(o, field);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new MappingException(field + " is out of range: " + value);
        }
        return (int) value;
    }

    private static BigDecimal decimal(JsonNode o, String field) {
        return optionalDecimal(o, field).orElse(BigDecimal.ZERO);
    }

    private static Optional<BigDecimal> optionalDecimal(JsonNode o, String field) {
        JsonNode node = o.get(field);
        if (isAbsent(node) || (node.isTextual() && node.textValue().isBlank())) {
            return Optional.empty();
        }
        return Optional.of(toDecimal(node, field));
    }

    /** Amounts arrive as JSON numbers from some endpoints and as strings from others. */
    private static BigDecimal toDecimal(JsonNode node, String name) {
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            try {
                return new BigDecimal(node.textValue().trim());
            } catch (NumberFormatException e) {
                throw new MappingException(name + " is not a number: " + node, e);
            }
        }
        throw new MappingException(name + " is not a number: " + node);
    }

    private static boolean bool(JsonNode o, String field) {
        JsonNode node = o.get(field);
        if (isAbsent(node)) {
            return false;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isNumber()) {
            return node.decimalValue().signum() != 0;
        }
        if (node.isTextual()) {
            String text = node.textValue().trim();
            if (text.equals("true") || text.equals("1")) {
                return true;
            }
            if (text.isEmpty() || text.equals("false") || text.equals("0")) {
                return false;
            }
        }
        throw new MappingException(field + " is not a boolean: " + node);
    }

    private static Optional<Timestamp> timestamp(JsonNode o, String field) {
        JsonNode node = o.get(field);
        if (isAbsent(node)) {
            return Optional.empty();
        }
        if (node.isTextual() || node.isIntegralNumber()) {
            String raw = node.asText();
            return raw.isBlank() ? Optional.empty() : Optional.of(Timestamp.parse(raw));
        }
        throw new MappingException(field + " is not a timestamp: " + node);
    }

    static final class MappingException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        MappingException(String message) {
            super(message);
        }

        MappingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
