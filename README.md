# Tron Energy Rental via API
## Java SDK by TronZap.com

**[English](README.md)** | [Español](README.es.md) | [Português](README.pt-br.md) | [Русский](README.ru.md)

[![Maven Central](https://img.shields.io/maven-central/v/com.tronzap/tronzap-java.svg)](https://central.sonatype.com/artifact/com.tronzap/tronzap-java)
[![CI](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Official Java SDK for the TronZap API.
This SDK allows you to easily integrate with TronZap services for TRON energy rental.

TronZap.com allows you to [buy TRON energy](https://tronzap.com/), making USDT (TRC20) transfers cheaper by significantly reducing transaction fees.

👉 [Register for an API key](https://tronzap.com) to start using TronZap API and integrate it via the SDK.

- Website: https://tronzap.com
- API reference: https://docs.tronzap.com/
- Maven Central: https://central.sonatype.com/artifact/com.tronzap/tronzap-java

## Installation

Maven:

```xml
<dependency>
    <groupId>com.tronzap</groupId>
    <artifactId>tronzap-java</artifactId>
    <version>1.1.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("com.tronzap:tronzap-java:1.1.0")
```

## Requirements

- Java 17 or newer
- One runtime dependency, Jackson Databind, used internally. No Jackson type appears in the SDK's public API.

## Quick start

```java
import com.tronzap.sdk.TronzapClient;
import com.tronzap.sdk.exception.TronzapException;
import com.tronzap.sdk.request.EnergyTransactionRequest;
import com.tronzap.sdk.request.EstimateEnergyRequest;
import com.tronzap.sdk.response.Balance;
import com.tronzap.sdk.response.EnergyEstimate;
import com.tronzap.sdk.response.Transaction;

public class QuickStart {
    public static void main(String[] args) {
        TronzapClient client = TronzapClient.builder()
                .apiToken("your_api_token")
                .apiSecret("your_api_secret")
                .build();

        try {
            Balance balance = client.getBalance();
            System.out.println("balance: " + balance.balance() + " (deposit to " + balance.address() + ")");

            // Estimate how much energy a USDT transfer needs, then buy exactly that much.
            EnergyEstimate estimate = client.estimateEnergy(
                    EstimateEnergyRequest.of("TSenderAddress", "TRecipientAddress"));

            Transaction tx = client.createEnergyTransaction(
                    EnergyTransactionRequest.builder("TRecipientAddress", estimate.amount())
                            .duration(1)
                            .externalId("order-42")
                            .activateAddress(true)
                            .build());
            System.out.println("transaction " + tx.id() + " costs " + tx.amount() + " and is " + tx.status());
        } catch (TronzapException e) {
            System.err.println("TronZap call failed: " + e.getMessage());
        }
    }
}
```

A runnable walkthrough of every operation lives in
[`BasicUsage.java`](src/test/java/com/tronzap/sdk/example/BasicUsage.java):

```bash
export TRONZAP_API_TOKEN=your_api_token
export TRONZAP_API_SECRET=your_api_secret
export TRONZAP_BASE_URL=api.tronzap.com   # optional
./mvnw -q test-compile exec:java -Dexec.mainClass=com.tronzap.sdk.example.BasicUsage -Dexec.classpathScope=test
```

By default it only reads and spends nothing. Setting `TRONZAP_ALLOW_PURCHASES=1`
also exercises the endpoints that create transactions and AML checks, which debit
the account balance. See the comment at the top of the file for the other optional
variables.

## Configuration

The builder takes the two credentials from your dashboard: the API token is sent
as a bearer token, and the API secret signs every request body. Everything else is
optional:

```java
TronzapClient client = TronzapClient.builder()
        .apiToken(apiToken)
        .apiSecret(apiSecret)
        .baseUrl("api.tronzap.com")              // defaults to TronzapClient.DEFAULT_BASE_URL
        .timeout(Duration.ofSeconds(10))         // whole request; defaults to 30 seconds
        .connectTimeout(Duration.ofSeconds(5))   // defaults to 10 seconds
        .userAgent("my-app/1.0")
        .build();
```

`baseUrl` takes either a bare domain or a full URL: a missing scheme becomes
`https` and a trailing slash is trimmed, so `"api.tronzap.com"`,
`"api.tronzap.com/"` and `"https://api.tronzap.com"` are equivalent. Pass an
explicit scheme to opt out, for example `"http://localhost:8080"` against a local
mock.

To use a proxy, your own executor or a custom `SSLContext`, pass your own
`java.net.http.HttpClient`. It is used as given and never closed. Set the connect
timeout on that client, because `connectTimeout` only configures the client the
builder creates:

```java
HttpClient httpClient = HttpClient.newBuilder()
        .proxy(ProxySelector.of(new InetSocketAddress("proxy.internal", 3128)))
        .connectTimeout(Duration.ofSeconds(5))
        .build();

TronzapClient client = TronzapClient.builder()
        .apiToken(apiToken)
        .apiSecret(apiSecret)
        .httpClient(httpClient)
        .build();
```

A `TronzapClient` is immutable, safe for concurrent use and holds no global state,
so create one per set of credentials and share it. `timeout` bounds the whole
request, including a response body that arrives slowly.

## Available methods

| Method | Endpoint | Description |
|---|---|---|
| `getServices()` | `/v1/services` | Available services and prices |
| `getBalance()` | `/v1/balance` | Current account balance |
| `getAddressInfo(address)` | `/v1/address-info` | Address resources (energy, bandwidth) and balances (TRX, USDT) |
| `estimateEnergy(request)` | `/v1/estimate-energy` | Energy a transfer needs, and its cost |
| `calculate(request)` | `/v1/calculate` | Price a purchase without creating a transaction |
| `createEnergyTransaction(request)` | `/v1/transaction/new` | Buy energy |
| `createBandwidthTransaction(request)` | `/v1/transaction/new` | Buy bandwidth |
| `createResourceBundleTransaction(request)` | `/v1/transaction/new` | Buy energy and bandwidth in one transaction |
| `createAddressActivationTransaction(request)` | `/v1/transaction/new` | Activate a TRON address |
| `checkTransaction(request)` | `/v1/transaction/check` | Status of a transaction, by id or external id |
| `getDirectRechargeInfo()` | `/v1/direct-recharge-info` | Direct recharge address and rates |
| `getAmlServices()` | `/v1/aml-checks` | AML services and pricing |
| `createAmlCheck(request)` | `/v1/aml-checks/new` | Start an AML screening |
| `checkAmlStatus(id)` | `/v1/aml-checks/check` | Status and result of an AML check |
| `getAmlHistory()` / `getAmlHistory(request)` | `/v1/aml-checks/history` | Paginated AML check history |
| `getSubscriptions()` | `/v1/subscriptions` | Subscription plans and prices |
| `startSubscription(request)` | `/v1/subscription/start` | Subscribe an address to a plan |
| `checkSubscription(request)` | `/v1/subscription/check` | Status of a subscription, by id or external id |
| `stopSubscription(request)` | `/v1/subscription/stop` | Stop a subscription |
| `getSubscriptionHistory()` / `getSubscriptionHistory(request)` | `/v1/subscriptions/history` | Paginated subscription history |

Parameters live in immutable request records in `com.tronzap.sdk.request`. Each
has an `of(...)` factory for the required values, and the ones with several
optional values also have a `builder(...)`. A request validates itself when it is
created, so an invalid one is never sent. Defaults match the API: `duration` is 1
hour, and AML and subscription history start at page 1 with 10 items. The
exception is `StartSubscriptionRequest`, where a zero `durationDays` or
`transactionsLimit` means no limit.

Results are immutable records in `com.tronzap.sdk.response`. Collections are never
`null`, and values the API may omit are `Optional`.

### Buying resources

```java
// Energy, optionally activating the address in the same call.
Transaction tx = client.createEnergyTransaction(
        EnergyTransactionRequest.builder("TRecipientAddress", 65000)
                .duration(1)          // hours; only 1 is supported
                .externalId("order-42")
                .activateAddress(true)
                .build());

// Bandwidth.
tx = client.createBandwidthTransaction(
        BandwidthTransactionRequest.of("TRecipientAddress", 345, "bandwidth-1"));

// Energy and bandwidth together in one transaction.
tx = client.createResourceBundleTransaction(
        ResourceBundleTransactionRequest.builder("TRecipientAddress", 65000, 345)
                .externalId("bundle-1")
                .build());

// Activation on its own.
tx = client.createAddressActivationTransaction(
        AddressActivationRequest.of("TRecipientAddress", "activation-1"));
```

Prices in `getServices()` are per 1000 units for both energy and bandwidth: 65000
energy at an `EnergyRate.price()` of 0.03 costs 0.03 × 65000 / 1000 = 1.95, and
345 bandwidth at a `BandwidthRate.price()` of 1 costs 0.345.

The API currently reports a resource bundle with `service()` equal to
`Service.ENERGY`, not `Service.RESOURCE_BUNDLE`. Read `params().amounts()` to see
which resources a transaction contains.

### Following a transaction

A transaction moves through `NEW` → `PENDING` → `SUCCESS` or `FAILED`:

```java
Transaction tx;
do {
    Thread.sleep(2000);
    tx = client.checkTransaction(CheckTransactionRequest.byExternalId("order-42"));
} while (tx.status() == TransactionStatus.NEW || tx.status() == TransactionStatus.PENDING);

System.out.println("finished as " + tx.status() + ", hash " + tx.hash().orElse("none"));
```

### AML screening

```java
AmlCheck check = client.createAmlCheck(AmlCheckRequest.forAddress("TRX", "TAddressToScreen"));
// or AmlCheckRequest.forHash("BTC", "bc1RecipientAddress", "TX_HASH", AmlDirection.WITHDRAWAL)

AmlCheck result = client.checkAmlStatus(check.id());
if (result.status() == AmlStatus.COMPLETED) {
    System.out.println(result.riskLevel() + " " + result.riskScore() + " " + result.riskFactors());
}
```

For a hash check, `address` is the recipient address of the transaction, where
the funds were received, and the direction says which side you are on: `DEPOSIT`
if the funds were sent to your address (`address` is your address), `WITHDRAWAL`
if you sent them (`address` is the external recipient's address). The risk is
scored for the counterparty: the sender of a deposit, the recipient of a
withdrawal. When you omit the direction, as in `forHash(network, address, hash)`,
the SDK sends `DEPOSIT`.

`riskScore()` is empty until screening finishes. A completed check can have a
score of 0, which is not the same as having no score yet.

### Subscriptions

A subscription keeps an address supplied with energy for every transaction until
it is stopped or runs out of days or transactions. Pick a plan from
`getSubscriptions()` and pass its `subscriptionId()`, such as `"unlimited_energy"`,
not its numeric `id()`. Starting a subscription charges the plan's initial price.

```java
List<SubscriptionPlan> plans = client.getSubscriptions();
for (SubscriptionPlan plan : plans) {
    System.out.println(plan.subscriptionId() + " " + plan.initialPrice() + " " + plan.price());
}

Subscription sub = client.startSubscription(
        StartSubscriptionRequest.builder("unlimited_energy", "TRecipientAddress")
                .durationDays(30)         // 0 for no time limit
                .transactionsLimit(0)     // 0 for no limit
                .externalId("subscription-42")
                .build());

sub = client.checkSubscription(SubscriptionRequest.byExternalId("subscription-42"));

sub = client.stopSubscription(SubscriptionRequest.byId(sub.id()));

SubscriptionHistory history = client.getSubscriptionHistory(
        SubscriptionHistoryRequest.of(1, 10, SubscriptionStatus.ACTIVE));
```

Start, check and stop return the subscription with its `params()`; the history
returns the usage counters `transactionsUsed()`, `energyUsed()` and `totalPrice()`
instead, and an empty `params()`. A subscription with a transactions limit cannot
be stopped (`CANNOT_STOP_SUBSCRIPTION`).

## Error handling

Every failure of an API call is an unchecked `TronzapException`. Catch a subclass
to handle one kind of failure:

```
TronzapException
├── ApiException                  — the API answered with a non-zero code
├── HttpException                 — non-2xx response without an API payload
│   ├── RateLimitException        — HTTP 429
│   ├── UnauthorizedException     — HTTP 401 or 403
│   └── ServerException           — HTTP 5xx
├── InvalidResponseException      — 2xx response the SDK could not read
└── NetworkException              — no response arrived
    ├── ConnectionException       — DNS failure, connection refused
    ├── RequestTimeoutException   — the request exceeded its timeout
    ├── SslException              — TLS handshake or certificate failure
    └── RequestInterruptedException — the calling thread was interrupted
```

`ApiException`, `HttpException` and `InvalidResponseException` carry the HTTP
status (`getStatusCode()`) and the raw response body (`getResponseBody()`).
`ApiException` also carries the API error code, the error key and the request ID.
Invalid arguments raise `IllegalArgumentException` before anything is sent.

```java
try {
    client.createEnergyTransaction(EnergyTransactionRequest.of("TRecipientAddress", 65000));
} catch (ApiException e) {
    // Application-level failure: the code says exactly what went wrong.
    switch (e.getErrorCode()) {
        case INVALID_TRON_ADDRESS ->
                // The key may narrow it down, e.g. "invalid_tron_address.from_address"
                System.err.println("bad address: " + e.getErrorKey().orElse(""));
        case INSUFFICIENT_FUNDS -> System.err.println("top up the account");
        case ADDRESS_NOT_ACTIVATED -> System.err.println("activate the address first");
        default -> System.err.printf("api error %d: %s (request %s)%n",
                e.getCode(), e.getMessage(), e.getRequestId().orElse("-"));
    }
} catch (RateLimitException e) {
    // Back off and retry.
} catch (UnauthorizedException e) {
    // Bad token or signature.
} catch (RequestTimeoutException | ServerException e) {
    // Transient; safe to retry.
} catch (NetworkException e) {
    // Unreachable.
}
```

`getRequestId()` is the identifier the API assigns to each request. Quote it
when contacting support.

An API error takes precedence over the HTTP status: the API reports some failures
with a 2xx status and others with a 4xx or 5xx status, so a readable payload with
a non-zero code is always reported as `ApiException`, never as `HttpException`.

### API error codes

| Code | Constant | Description |
|------|----------|-------------|
| 1 | `AUTH_ERROR` | Authentication error – invalid API token or signature |
| 2 | `INVALID_SERVICE_OR_PARAMS` | Invalid service or parameters |
| 5 | `WALLET_NOT_FOUND` | Internal wallet not found. Contact support. |
| 6 | `INSUFFICIENT_FUNDS` | Insufficient funds |
| 10 | `INVALID_TRON_ADDRESS` | Invalid TRON address, or the address already has an active subscription |
| 11 | `INVALID_ENERGY_AMOUNT` | Invalid energy amount |
| 12 | `INVALID_DURATION` | Invalid duration |
| 20 | `TRANSACTION_NOT_FOUND` | Transaction/subscription not found |
| 21 | `CANNOT_STOP_SUBSCRIPTION` | Cannot stop subscription, e.g. it has a transactions limit |
| 24 | `ADDRESS_NOT_ACTIVATED` | Address not activated |
| 25 | `ADDRESS_ALREADY_ACTIVATED` | Address already activated |
| 30 | `AML_CHECK_NOT_FOUND` | AML check not found |
| 35 | `SERVICE_NOT_AVAILABLE` | Service not available |
| 50 | `INVALID_BANDWIDTH_AMOUNT` | Invalid bandwidth amount |
| 500 | `INTERNAL_SERVER_ERROR` | Internal server error – contact support |

The constants are values of the `ApiErrorCode` enum. A code this SDK version does
not know is reported as `ApiErrorCode.UNKNOWN`, with the number still available
from `getCode()`.

## Decimal and timestamp fields

Amounts and prices are `BigDecimal`, keeping the scale the API sent, so compare
them with `compareTo` rather than `equals`. The API encodes money as a JSON number
in some responses and as a JSON string in others; both forms are read the same
way.

Timestamps are `Timestamp` values: `value()` is the parsed `OffsetDateTime` and
`raw()` is the text exactly as the API sent it. The several formats the API emits
are accepted, and times without an offset are read as UTC. An unrecognised
timestamp leaves `value()` empty instead of failing the whole response.

Values the API may add in the future, such as a new transaction status, are
reported as the `UNKNOWN` constant of the matching enum instead of failing.

## Testing

```bash
./mvnw verify
```

It runs the unit tests against a local HTTP server, checks the Javadoc and the
test coverage, and builds the main, sources and Javadoc JARs.

## License

The MIT License (MIT). Please see [License File](LICENSE) for more information.

## Support

For support, please contact [support@tronzap.com](mailto:support@tronzap.com).
