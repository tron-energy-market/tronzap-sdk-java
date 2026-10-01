package com.tronzap.sdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import javax.net.ssl.SSLContext;

final class TestServer implements AutoCloseable {

    static final String TOKEN = "test-token";
    static final String SECRET = "test-secret";

    record Received(String method, String path, Map<String, List<String>> headers, byte[] body) {

        String bodyText() {
            return new String(body, StandardCharsets.UTF_8);
        }

        String header(String name) {
            return headers.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                    .map(entry -> entry.getValue().get(0))
                    .findFirst()
                    .orElse(null);
        }
    }

    record Reply(int status, String body) {
    }

    @FunctionalInterface
    interface Handler {
        void handle(HttpExchange exchange, Received received) throws IOException;
    }

    private final HttpServer server;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ConcurrentLinkedQueue<Received> received = new ConcurrentLinkedQueue<>();
    private final String scheme;
    private volatile Handler handler = (exchange, request) -> write(exchange, 200, ok("{}"));

    private TestServer(HttpServer server, String scheme) {
        this.server = server;
        this.scheme = scheme;
        server.setExecutor(executor);
        server.createContext("/", this::dispatch);
        server.start();
    }

    static TestServer start() throws IOException {
        return new TestServer(HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0), "http");
    }

    static TestServer startTls(SSLContext sslContext) throws IOException {
        HttpsServer server = HttpsServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        return new TestServer(server, "https");
    }

    static String ok(String result) {
        return "{\"code\":0,\"request_id\":\"req-1\",\"result\":" + result + "}";
    }

    static void write(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    String url() {
        return scheme + "://127.0.0.1:" + server.getAddress().getPort();
    }

    TronzapClientBuilder clientBuilder() {
        return TronzapClient.builder().apiToken(TOKEN).apiSecret(SECRET).baseUrl(url());
    }

    TronzapClient client() {
        return clientBuilder().build();
    }

    TestServer reply(int status, String body) {
        handler = (exchange, request) -> write(exchange, status, body);
        return this;
    }

    TestServer replyOk(String result) {
        return reply(200, ok(result));
    }

    TestServer reply(Function<Received, Reply> replies) {
        handler = (exchange, request) -> {
            Reply reply = replies.apply(request);
            write(exchange, reply.status(), reply.body());
        };
        return this;
    }

    TestServer handler(Handler handler) {
        this.handler = handler;
        return this;
    }

    List<Received> requests() {
        return List.copyOf(received);
    }

    Received onlyRequest() {
        List<Received> all = requests();
        if (all.size() != 1) {
            throw new AssertionError("expected exactly one request, got " + all.size());
        }
        return all.get(0);
    }

    private void dispatch(HttpExchange exchange) throws IOException {
        try (exchange) {
            byte[] body = exchange.getRequestBody().readAllBytes();
            Received request = new Received(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    Map.copyOf(exchange.getRequestHeaders()),
                    body);
            received.add(request);
            handler.handle(exchange, request);
        }
    }

    @Override
    public void close() {
        server.stop(0);
        executor.shutdownNow();
    }
}
