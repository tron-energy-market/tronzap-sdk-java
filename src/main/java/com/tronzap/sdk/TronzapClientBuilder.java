package com.tronzap.sdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/**
 * Configures and creates a {@link TronzapClient}. Obtain one with {@link TronzapClient#builder()}.
 *
 * <pre>{@code
 * TronzapClient client = TronzapClient.builder()
 *     .apiToken(System.getenv("TRONZAP_API_TOKEN"))
 *     .apiSecret(System.getenv("TRONZAP_API_SECRET"))
 *     .timeout(Duration.ofSeconds(10))
 *     .build();
 * }</pre>
 *
 * <p>A builder is not safe for concurrent use; the client it builds is.
 */
public final class TronzapClientBuilder {

    private String apiToken;
    private String apiSecret;
    private String baseUrl = TronzapClient.DEFAULT_BASE_URL;
    private Duration timeout = TronzapClient.DEFAULT_TIMEOUT;
    private Duration connectTimeout;
    private HttpClient httpClient;
    private String userAgent = TronzapClient.DEFAULT_USER_AGENT;

    TronzapClientBuilder() {
    }

    /**
     * Sets the API token, sent as a bearer token with every request. Required.
     *
     * @param apiToken the API token from your TronZap dashboard
     * @return this builder
     */
    public TronzapClientBuilder apiToken(String apiToken) {
        this.apiToken = Objects.requireNonNull(apiToken, "apiToken");
        return this;
    }

    /**
     * Sets the API secret used to sign every request body. Required. The secret itself is never sent.
     *
     * @param apiSecret the API secret from your TronZap dashboard
     * @return this builder
     */
    public TronzapClientBuilder apiSecret(String apiSecret) {
        this.apiSecret = Objects.requireNonNull(apiSecret, "apiSecret");
        return this;
    }

    /**
     * Points the client at a different API host. The default is {@value TronzapClient#DEFAULT_BASE_URL}.
     *
     * <p>A bare host such as {@code api.tronzap.com} gets the {@code https} scheme, and a trailing slash
     * is removed. Give an explicit scheme to opt out, for example {@code http://localhost:8080} when
     * testing against a local server.
     *
     * @param baseUrl the base URL or host name
     * @return this builder
     */
    public TronzapClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        return this;
    }

    /**
     * Sets the deadline for a whole request, from sending it to reading the last byte of the response.
     * The default is 30 seconds.
     *
     * @param timeout a positive duration
     * @return this builder
     * @throws IllegalArgumentException if the duration is zero or negative
     */
    public TronzapClientBuilder timeout(Duration timeout) {
        this.timeout = positive(timeout, "timeout");
        return this;
    }

    /**
     * Sets the deadline for establishing a connection. The default is 10 seconds.
     *
     * <p>It configures the HTTP client this builder creates, so it cannot be combined with {@link
     * #httpClient(HttpClient)}; set the connect timeout on your own client instead.
     *
     * @param connectTimeout a positive duration
     * @return this builder
     * @throws IllegalArgumentException if the duration is zero or negative
     */
    public TronzapClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = positive(connectTimeout, "connectTimeout");
        return this;
    }

    /**
     * Sets the HTTP client used for every request, for example to configure a proxy, an executor or a
     * custom {@link javax.net.ssl.SSLContext}. The client is used as given and is shared, never closed.
     * By default the builder creates one.
     *
     * @param httpClient the HTTP client
     * @return this builder
     */
    public TronzapClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        return this;
    }

    /**
     * Overrides the {@code User-Agent} header sent with every request.
     *
     * @param userAgent a non-blank user agent
     * @return this builder
     * @throws IllegalArgumentException if the value is blank
     */
    public TronzapClientBuilder userAgent(String userAgent) {
        Objects.requireNonNull(userAgent, "userAgent");
        if (userAgent.isBlank()) {
            throw new IllegalArgumentException("userAgent must not be blank");
        }
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Creates the client.
     *
     * @return a new client
     * @throws IllegalStateException if the API token or secret is missing, or if {@link
     *     #connectTimeout(Duration)} was combined with {@link #httpClient(HttpClient)}
     * @throws IllegalArgumentException if the base URL is not a valid HTTP or HTTPS URL
     */
    public TronzapClient build() {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException("apiToken is required");
        }
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException("apiSecret is required");
        }
        if (httpClient != null && connectTimeout != null) {
            throw new IllegalStateException("connectTimeout cannot be combined with a custom httpClient; set it on that client");
        }
        HttpClient client = httpClient != null
                ? httpClient
                : HttpClient.newBuilder()
                        .connectTimeout(connectTimeout != null ? connectTimeout : TronzapClient.DEFAULT_CONNECT_TIMEOUT)
                        .build();
        return new TronzapClient(apiToken, apiSecret, normalizeBaseUrl(baseUrl), timeout, client, userAgent);
    }

    static String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        if (!normalized.contains("://")) {
            normalized = "https://" + (normalized.startsWith("//") ? normalized.substring(2) : normalized);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("baseUrl is not a valid URL: " + baseUrl, e);
        }
        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("baseUrl must use http or https: " + baseUrl);
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("baseUrl has no host: " + baseUrl);
        }
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("baseUrl must not have a query or fragment: " + baseUrl);
        }
        return normalized;
    }

    private static Duration positive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive, got " + duration);
        }
        return duration;
    }
}
