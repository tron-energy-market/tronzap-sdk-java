package com.tronzap.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tronzap.sdk.exception.ApiException;
import com.tronzap.sdk.exception.HttpException;
import com.tronzap.sdk.exception.InvalidResponseException;
import com.tronzap.sdk.exception.NetworkException;
import com.tronzap.sdk.exception.RequestInterruptedException;
import com.tronzap.sdk.exception.RequestTimeoutException;
import com.tronzap.sdk.exception.TronzapException;
import com.tronzap.sdk.model.Service;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Client for the <a href="https://docs.tronzap.com/">TronZap API</a>: buy TRON energy and bandwidth,
 * activate addresses, manage energy subscriptions and run AML checks.
 *
 * <pre>{@code
 * TronzapClient client = TronzapClient.builder()
 *     .apiToken(System.getenv("TRONZAP_API_TOKEN"))
 *     .apiSecret(System.getenv("TRONZAP_API_SECRET"))
 *     .build();
 *
 * Balance balance = client.getBalance();
 * Transaction tx = client.createEnergyTransaction(EnergyTransactionRequest.of("TRX_ADDRESS", 65000));
 * }</pre>
 *
 * <p>A client is immutable and safe for concurrent use. Create one and share it: it holds no state
 * between requests beyond the {@link HttpClient} it was given.
 *
 * <p>Every method sends one signed request and blocks until the response arrives or the {@linkplain
 * TronzapClientBuilder#timeout(Duration) timeout} elapses. A failure is reported as a {@link
 * TronzapException}: an {@link ApiException} when the API rejected the request, an {@link
 * HttpException} for a non-2xx response without an API payload, an {@link InvalidResponseException}
 * for a response the SDK cannot read, and a {@link NetworkException} when no response arrived.
 * Invalid arguments raise {@link IllegalArgumentException} before anything is sent.
 */
public final class TronzapClient {

    /** The SDK version, reported in the default {@code User-Agent} header. */
    public static final String VERSION = "1.0.0";

    /** The production API endpoint. */
    public static final String DEFAULT_BASE_URL = "https://api.tronzap.com";

    /** The request timeout used unless {@link TronzapClientBuilder#timeout(Duration)} overrides it. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /** The connect timeout used unless {@link TronzapClientBuilder#connectTimeout(Duration)} overrides it. */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    static final String DEFAULT_USER_AGENT = "tronzap-sdk-java/" + VERSION;

    static final long MAX_RESPONSE_BYTES = 8L * 1024 * 1024;

    private static final HexFormat HEX = HexFormat.of();

    private final String apiToken;
    private final byte[] apiSecret;
    private final String baseUrl;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final String userAgent;
    private final JsonCodec codec = new JsonCodec();

    TronzapClient(String apiToken, String apiSecret, String baseUrl, Duration timeout, HttpClient httpClient, String userAgent) {
        this.apiToken = apiToken;
        this.apiSecret = apiSecret.getBytes(StandardCharsets.UTF_8);
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.httpClient = httpClient;
        this.userAgent = userAgent;
    }

    /**
     * Starts configuring a new client.
     *
     * @return a new builder
     */
    public static TronzapClientBuilder builder() {
        return new TronzapClientBuilder();
    }

    /**
     * Returns the resources on sale and their current prices.
     *
     * @return the services and their price tiers
     * @throws TronzapException if the request fails
     */
    public Services getServices() {
        return call("/v1/services", codec.object(), ResultMapper::services);
    }

    /**
     * Returns the available AML screening products and their prices.
     *
     * @return the AML services, possibly empty
     * @throws TronzapException if the request fails
     */
    public List<AmlService> getAmlServices() {
        return call("/v1/aml-checks", codec.object(), ResultMapper::amlServices);
    }

    /**
     * Returns the account balance and the address it deposits to.
     *
     * @return the balance
     * @throws TronzapException if the request fails
     */
    public Balance getBalance() {
        return call("/v1/balance", codec.object(), ResultMapper::balance);
    }

    /**
     * Returns the on-chain resources (energy, bandwidth) and token balances (TRX, USDT) of an address.
     *
     * @param address the TRON address to query
     * @return the address resources and balances
     * @throws IllegalArgumentException if the address is missing or blank
     * @throws TronzapException if the request fails
     */
    public AddressInfo getAddressInfo(String address) {
        ObjectNode params = codec.object().put("address", required(address, "address"));
        return call("/v1/address-info", params, ResultMapper::addressInfo);
    }

    /**
     * Estimates how much energy a token transfer needs and what that energy costs. Without a contract
     * address the estimate is for a USDT (TRC20) transfer.
     *
     * <p>The API does not reject a request whose sender and recipient are the same address.
     *
     * @param request the transfer to estimate
     * @return the energy estimate
     * @throws TronzapException if the request fails
     */
    public EnergyEstimate estimateEnergy(EstimateEnergyRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object()
                .put("from_address", request.fromAddress())
                .put("to_address", request.toAddress());
        request.contractAddress().ifPresent(contract -> params.put("contract_address", contract));
        return call("/v1/estimate-energy", params, ResultMapper::energyEstimate);
    }

    /**
     * Prices an energy purchase without creating a transaction.
     *
     * @param request the purchase to price
     * @return the price
     * @throws TronzapException if the request fails
     */
    public Calculation calculate(CalculateRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object()
                .put("address", request.address())
                .put("amount", request.energy())
                .put("duration", request.duration());
        return call("/v1/calculate", params, ResultMapper::calculation);
    }

    /**
     * Buys energy for an address, optionally activating the address in the same transaction.
     *
     * @param request the purchase
     * @return the created transaction
     * @throws TronzapException if the request fails
     */
    public Transaction createEnergyTransaction(EnergyTransactionRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object().put("address", request.address());
        params.putObject("amounts").put("energy", request.energy());
        params.put("duration", request.duration());
        if (request.activateAddress()) {
            params.put("activate_address", true);
        }
        return createTransaction(Service.ENERGY, params, request.externalId());
    }

    /**
     * Buys bandwidth for an address. Bandwidth is rented for one hour.
     *
     * @param request the purchase
     * @return the created transaction
     * @throws TronzapException if the request fails
     */
    public Transaction createBandwidthTransaction(BandwidthTransactionRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object().put("address", request.address());
        params.putObject("amounts").put("bandwidth", request.bandwidth());
        params.put("duration", 1);
        return createTransaction(Service.BANDWIDTH, params, request.externalId());
    }

    /**
     * Buys energy and bandwidth for an address in one transaction, optionally activating the address.
     *
     * @param request the purchase
     * @return the created transaction
     * @throws TronzapException if the request fails
     */
    public Transaction createResourceBundleTransaction(ResourceBundleTransactionRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object().put("address", request.address());
        params.putObject("amounts").put("energy", request.energy()).put("bandwidth", request.bandwidth());
        params.put("duration", request.duration());
        if (request.activateAddress()) {
            params.put("activate_address", true);
        }
        return createTransaction(Service.RESOURCE_BUNDLE, params, request.externalId());
    }

    /**
     * Activates a TRON address. An address must be activated once before it can hold resources.
     *
     * <p>Activating an address that is already active fails with {@link ApiException} and code {@link
     * com.tronzap.sdk.exception.ApiErrorCode#ADDRESS_ALREADY_ACTIVATED}.
     *
     * @param request the address to activate
     * @return the created transaction
     * @throws TronzapException if the request fails
     */
    public Transaction createAddressActivationTransaction(AddressActivationRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object().put("address", request.address());
        return createTransaction(Service.ACTIVATE_ADDRESS, params, request.externalId());
    }

    /**
     * Returns the current state of a transaction.
     *
     * @param request the transaction ID, external ID, or both
     * @return the transaction
     * @throws TronzapException if the request fails; an unknown transaction fails with code {@link
     *     com.tronzap.sdk.exception.ApiErrorCode#TRANSACTION_NOT_FOUND}
     */
    public Transaction checkTransaction(CheckTransactionRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object();
        request.id().ifPresent(id -> params.put("id", id));
        request.externalId().ifPresent(externalId -> params.put("external_id", externalId));
        return call("/v1/transaction/check", params, ResultMapper::transaction);
    }

    /**
     * Returns the address to pay for direct energy recharge and the rates energy is delivered at.
     *
     * @return the direct recharge information
     * @throws TronzapException if the request fails
     */
    public DirectRechargeInfo getDirectRechargeInfo() {
        return call("/v1/direct-recharge-info", codec.object(), ResultMapper::directRechargeInfo);
    }

    /**
     * Starts an AML screening of an address or a transaction hash. Screening runs asynchronously: poll
     * {@link #checkAmlStatus(String)} until the status is {@link
     * com.tronzap.sdk.model.AmlStatus#COMPLETED}.
     *
     * @param request what to screen
     * @return the created AML check
     * @throws TronzapException if the request fails
     */
    public AmlCheck createAmlCheck(AmlCheckRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object()
                .put("type", request.type().value())
                .put("network", request.network())
                .put("address", request.address());
        request.hash().ifPresent(hash -> params.put("hash", hash));
        request.direction().ifPresent(direction -> params.put("direction", direction.value()));
        return call("/v1/aml-checks/new", params, ResultMapper::amlCheck);
    }

    /**
     * Returns the current state of an AML check and, once it is complete, its result.
     *
     * @param id the AML check ID
     * @return the AML check
     * @throws IllegalArgumentException if the ID is missing or blank
     * @throws TronzapException if the request fails
     */
    public AmlCheck checkAmlStatus(String id) {
        ObjectNode params = codec.object().put("id", required(id, "id"));
        return call("/v1/aml-checks/check", params, ResultMapper::amlCheck);
    }

    /**
     * Returns the first page of past AML checks, ten per page.
     *
     * @return the first page of AML checks
     * @throws TronzapException if the request fails
     */
    public AmlHistory getAmlHistory() {
        return getAmlHistory(AmlHistoryRequest.firstPage());
    }

    /**
     * Returns one page of past AML checks, newest first.
     *
     * @param request the page to return and an optional status filter
     * @return the page of AML checks
     * @throws TronzapException if the request fails
     */
    public AmlHistory getAmlHistory(AmlHistoryRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object()
                .put("page", request.page())
                .put("per_page", request.perPage());
        request.status().ifPresent(status -> params.put("status", status.value()));
        return call("/v1/aml-checks/history", params, ResultMapper::amlHistory);
    }

    /**
     * Returns the subscription plans on sale, in the order the API lists them.
     *
     * @return the plans, possibly empty
     * @throws TronzapException if the request fails
     */
    public List<SubscriptionPlan> getSubscriptions() {
        return call("/v1/subscriptions", codec.object(), ResultMapper::subscriptionPlans);
    }

    /**
     * Subscribes an address to a plan from {@link #getSubscriptions()}. Starting a subscription charges
     * the plan's initial price.
     *
     * <p>Starting a subscription for an address that already has an active one fails with {@link
     * ApiException} and code {@link com.tronzap.sdk.exception.ApiErrorCode#INVALID_TRON_ADDRESS}.
     *
     * @param request the plan, the address and the limits
     * @return the started subscription
     * @throws TronzapException if the request fails
     */
    public Subscription startSubscription(StartSubscriptionRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object().put("subscription_id", request.subscriptionId());
        request.externalId().ifPresent(id -> params.put("external_id", id));
        ObjectNode subscriptionParams = params.putObject("params")
                .put("address", request.address())
                .put("duration", request.durationDays())
                .put("transactions_limit", request.transactionsLimit());
        if (request.activateAddress()) {
            subscriptionParams.put("activate_address", true);
        }
        return call("/v1/subscription/start", params, ResultMapper::subscription);
    }

    /**
     * Returns the current state of a subscription.
     *
     * @param request the subscription ID, external ID, or both
     * @return the subscription
     * @throws TronzapException if the request fails; an unknown subscription fails with code {@link
     *     com.tronzap.sdk.exception.ApiErrorCode#TRANSACTION_NOT_FOUND}
     */
    public Subscription checkSubscription(SubscriptionRequest request) {
        return subscriptionCall("/v1/subscription/check", request);
    }

    /**
     * Stops a subscription.
     *
     * @param request the subscription ID, external ID, or both
     * @return the stopped subscription
     * @throws TronzapException if the request fails; a subscription with a transactions limit cannot
     *     be stopped and fails with code {@link
     *     com.tronzap.sdk.exception.ApiErrorCode#CANNOT_STOP_SUBSCRIPTION}
     */
    public Subscription stopSubscription(SubscriptionRequest request) {
        return subscriptionCall("/v1/subscription/stop", request);
    }

    /**
     * Returns the first page of your subscriptions, ten per page.
     *
     * @return the first page of subscriptions
     * @throws TronzapException if the request fails
     */
    public SubscriptionHistory getSubscriptionHistory() {
        return getSubscriptionHistory(SubscriptionHistoryRequest.firstPage());
    }

    /**
     * Returns one page of your subscriptions, newest first.
     *
     * @param request the page to return and an optional status filter
     * @return the page of subscriptions
     * @throws TronzapException if the request fails
     */
    public SubscriptionHistory getSubscriptionHistory(SubscriptionHistoryRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object()
                .put("page", request.page())
                .put("per_page", request.perPage());
        request.status().ifPresent(status -> params.put("status", status.value()));
        return call("/v1/subscriptions/history", params, ResultMapper::subscriptionHistory);
    }

    private Subscription subscriptionCall(String endpoint, SubscriptionRequest request) {
        Objects.requireNonNull(request, "request");
        ObjectNode params = codec.object();
        request.id().ifPresent(id -> params.put("id", id));
        request.externalId().ifPresent(externalId -> params.put("external_id", externalId));
        return call(endpoint, params, ResultMapper::subscription);
    }

    private Transaction createTransaction(Service service, ObjectNode transactionParams, Optional<String> externalId) {
        ObjectNode params = codec.object().put("service", service.value());
        params.set("params", transactionParams);
        externalId.ifPresent(id -> params.put("external_id", id));
        return call("/v1/transaction/new", params, ResultMapper::transaction);
    }

    private <T> T call(String endpoint, ObjectNode params, Function<JsonNode, T> mapper) {
        byte[] body = codec.write(params);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiToken)
                .header("X-Signature", sign(body))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", userAgent)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<byte[]> response = send(request);
        JsonNode result = codec.decode(response.statusCode(), response.body());
        try {
            return mapper.apply(result);
        } catch (ResultMapper.MappingException e) {
            throw new InvalidResponseException(
                    "unexpected result in response: " + e.getMessage(),
                    response.statusCode(),
                    new String(response.body(), StandardCharsets.UTF_8),
                    e);
        }
    }

    /**
     * {@link HttpRequest#timeout()} only bounds the wait for response headers, so a server that
     * trickles the body would block forever; the overall deadline is enforced on the future instead.
     */
    private HttpResponse<byte[]> send(HttpRequest request) {
        CompletableFuture<HttpResponse<byte[]>> future =
                httpClient.sendAsync(request, new BoundedBodyHandler(MAX_RESPONSE_BYTES));
        try {
            return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RequestTimeoutException("request timed out after " + timeout.toMillis() + " ms", e);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new RequestInterruptedException("interrupted while waiting for the response", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof BoundedBodyHandler.ResponseTooLargeException tooLarge) {
                throw new InvalidResponseException(tooLarge.getMessage(), tooLarge.statusCode(), "", tooLarge);
            }
            throw NetworkErrors.classify(cause);
        }
    }

    private String sign(byte[] body) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
        digest.update(body);
        digest.update(apiSecret);
        return HEX.formatHex(digest.digest());
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
