package com.tronzap.sdk.example;

import com.tronzap.sdk.TronzapClient;
import com.tronzap.sdk.TronzapClientBuilder;
import com.tronzap.sdk.exception.ApiErrorCode;
import com.tronzap.sdk.exception.ApiException;
import com.tronzap.sdk.exception.TronzapException;
import com.tronzap.sdk.model.Timestamp;
import com.tronzap.sdk.request.AddressActivationRequest;
import com.tronzap.sdk.request.AmlCheckRequest;
import com.tronzap.sdk.request.BandwidthTransactionRequest;
import com.tronzap.sdk.request.CalculateRequest;
import com.tronzap.sdk.request.CheckTransactionRequest;
import com.tronzap.sdk.request.EnergyTransactionRequest;
import com.tronzap.sdk.request.EstimateEnergyRequest;
import com.tronzap.sdk.request.ResourceBundleTransactionRequest;
import com.tronzap.sdk.response.Transaction;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Walks through the TronZap API operations. By default it only reads and spends nothing.
 *
 * <pre>
 * export TRONZAP_API_TOKEN=your_api_token
 * export TRONZAP_API_SECRET=your_api_secret
 * export TRONZAP_BASE_URL=api.tronzap.com      # optional, e.g. a dev host
 * export TRONZAP_ADDRESS=TRON_ADDRESS          # optional
 * export TRONZAP_FROM_ADDRESS=TRON_ADDRESS     # optional, with TO_ADDRESS
 * export TRONZAP_TO_ADDRESS=TRON_ADDRESS       # optional, with FROM_ADDRESS
 * export TRONZAP_TRANSACTION_ID=id             # optional
 * export TRONZAP_AML_CHECK_ID=id               # optional
 * ./mvnw -q test-compile exec:java -Dexec.mainClass=com.tronzap.sdk.example.BasicUsage -Dexec.classpathScope=test
 * </pre>
 *
 * <p>Setting {@code TRONZAP_ALLOW_PURCHASES=1} additionally exercises the endpoints that create
 * transactions and AML checks. Those DEBIT THE ACCOUNT BALANCE. It is meant for verifying an
 * integration against a development environment, and it also needs {@code TRONZAP_ADDRESS}.
 */
public final class BasicUsage {

    private static final long ENERGY = 65000;
    private static final long BANDWIDTH = 345;

    private final TronzapClient client;
    private final List<String> failed = new ArrayList<>();

    private BasicUsage(TronzapClient client) {
        this.client = client;
    }

    public static void main(String[] args) {
        String token = System.getenv("TRONZAP_API_TOKEN");
        String secret = System.getenv("TRONZAP_API_SECRET");
        if (isBlank(token) || isBlank(secret)) {
            System.err.println("set TRONZAP_API_TOKEN and TRONZAP_API_SECRET");
            System.exit(2);
        }
        TronzapClientBuilder builder = TronzapClient.builder()
                .apiToken(token)
                .apiSecret(secret)
                .timeout(Duration.ofSeconds(20))
                .userAgent("tronzap-example/1.0");
        env("TRONZAP_BASE_URL").ifPresent(builder::baseUrl);
        System.out.println("Calling " + env("TRONZAP_BASE_URL").orElse(TronzapClient.DEFAULT_BASE_URL));

        BasicUsage example = new BasicUsage(builder.build());
        example.readOnly();
        if (env("TRONZAP_ALLOW_PURCHASES").filter("1"::equals).isPresent()) {
            example.purchases();
        } else {
            System.out.println("\nSkipping purchases: set TRONZAP_ALLOW_PURCHASES=1 to create transactions (debits the balance)");
        }

        if (!example.failed.isEmpty()) {
            System.err.println("\nFailed: " + String.join(", ", example.failed));
            System.exit(1);
        }
        System.out.println("\nAll calls succeeded");
    }

    private void readOnly() {
        step("getBalance", () -> {
            var balance = client.getBalance();
            System.out.printf("  balance %s, deposit address %s%n", balance.balance().toPlainString(), balance.address());
        });
        step("getServices", () -> {
            var services = client.getServices();
            services.energy().forEach(rate -> System.out.printf(
                    "  energy %dh %d..%d at %s per unit (65k = %s)%n",
                    rate.duration(), rate.minEnergy(), rate.maxEnergy(), rate.price().toPlainString(), rate.price65k().toPlainString()));
            services.bandwidth().forEach(rate -> System.out.printf(
                    "  bandwidth %dh %d..%d at %s per 1000 units%n",
                    rate.duration(), rate.minAmount(), rate.maxAmount(), rate.price().toPlainString()));
            services.activateAddress().ifPresent(rate -> System.out.println("  activation " + rate.price().toPlainString()));
        });
        step("getDirectRechargeInfo", () -> {
            var info = client.getDirectRechargeInfo();
            System.out.printf("  pay to %s, %d rate(s)%n", info.address(), info.rates().size());
        });
        step("getAmlServices", () -> client.getAmlServices().forEach(service -> System.out.printf(
                "  %s %s at %s%n", service.id(), service.type(), service.price().toPlainString())));
        step("getAmlHistory", () -> {
            var history = client.getAmlHistory();
            System.out.printf("  page %d, %d of %d check(s)%n", history.page(), history.items().size(), history.total());
        });

        Optional<String> address = env("TRONZAP_ADDRESS");
        optionalStep("getAddressInfo", address, value -> {
            var info = client.getAddressInfo(value);
            System.out.printf("  energy %d, bandwidth %d, balances %s%n",
                    info.resources().energy(), info.resources().bandwidth(), info.balances());
        });
        optionalStep("calculate", address, value -> {
            var calculation = client.calculate(CalculateRequest.of(value, ENERGY));
            System.out.printf("  %d energy for %dh costs %s%n",
                    calculation.energy(), calculation.duration(), calculation.total().toPlainString());
        });

        Optional<String> from = env("TRONZAP_FROM_ADDRESS");
        Optional<String> to = env("TRONZAP_TO_ADDRESS");
        optionalStep("estimateEnergy", from.isPresent() && to.isPresent() ? from : Optional.empty(), value -> {
            var estimate = client.estimateEnergy(EstimateEnergyRequest.of(value, to.orElseThrow()));
            System.out.printf("  %d energy, total %s%n", estimate.energy(), estimate.total().toPlainString());
        });
        optionalStep("checkTransaction", env("TRONZAP_TRANSACTION_ID"),
                value -> print(client.checkTransaction(CheckTransactionRequest.byId(value))));
        optionalStep("checkAmlStatus", env("TRONZAP_AML_CHECK_ID"), value -> {
            var check = client.checkAmlStatus(value);
            System.out.printf("  %s, risk %s%n", check.status(), check.riskScore().map(Object::toString).orElse("not scored yet"));
        });
    }

    private void purchases() {
        Optional<String> address = env("TRONZAP_ADDRESS");
        if (address.isEmpty()) {
            System.out.println("\nSkipping purchases: TRONZAP_ADDRESS is not set");
            return;
        }
        String target = address.get();
        String runId = "java-example-" + System.currentTimeMillis();

        step("createAddressActivationTransaction", () -> {
            try {
                print(client.createAddressActivationTransaction(AddressActivationRequest.of(target, runId + "-activate")));
            } catch (ApiException e) {
                if (e.getErrorCode() != ApiErrorCode.ADDRESS_ALREADY_ACTIVATED) {
                    throw e;
                }
                System.out.println("  already activated");
            }
        });
        step("createEnergyTransaction", () -> {
            Transaction created = client.createEnergyTransaction(
                    EnergyTransactionRequest.builder(target, ENERGY).externalId(runId + "-energy").build());
            print(created);
            print(client.checkTransaction(CheckTransactionRequest.byExternalId(runId + "-energy")));
        });
        step("createBandwidthTransaction", () ->
                print(client.createBandwidthTransaction(BandwidthTransactionRequest.of(target, BANDWIDTH, runId + "-bandwidth"))));
        step("createResourceBundleTransaction", () -> print(client.createResourceBundleTransaction(
                ResourceBundleTransactionRequest.builder(target, ENERGY, BANDWIDTH).externalId(runId + "-bundle").build())));
        step("createAmlCheck", () -> {
            var check = client.createAmlCheck(AmlCheckRequest.forAddress("TRX", target));
            System.out.printf("  AML check %s is %s%n", check.id(), check.status());
        });
    }

    private void step(String name, Runnable call) {
        System.out.println("\n" + name);
        try {
            call.run();
        } catch (TronzapException e) {
            System.out.println("  FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            failed.add(name);
        }
    }

    private void optionalStep(String name, Optional<String> subject, java.util.function.Consumer<String> call) {
        if (subject.isEmpty()) {
            System.out.println("\n" + name + "\n  skipped: its environment variable is not set");
            return;
        }
        step(name, () -> call.accept(subject.get()));
    }

    private static void print(Transaction tx) {
        System.out.printf("  %s %s %s, charged %s, created %s%n",
                tx.id(), tx.service(), tx.status(), tx.amount().toPlainString(),
                tx.createdAt().map(BasicUsage::describe).orElse("unknown"));
    }

    private static String describe(Timestamp timestamp) {
        return timestamp.value().map(Object::toString).orElse("UNPARSED(" + timestamp.raw() + ")");
    }

    private static Optional<String> env(String name) {
        return Optional.ofNullable(System.getenv(name)).filter(value -> !value.isBlank());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
