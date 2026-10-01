package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tronzap.sdk.request.EnergyTransactionRequest;
import com.tronzap.sdk.response.Transaction;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ConcurrencyTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void sharedClientKeepsConcurrentRequestsApart() throws Exception {
        int threads = 16;
        int perThread = 25;
        try (TestServer server = TestServer.start().reply(request -> {
            try {
                JsonNode body = JSON.readTree(request.body());
                String externalId = body.get("external_id").asText();
                String signature = RequestWireTest.sha256Hex(request.body(), TestServer.SECRET);
                if (!signature.equals(request.header("X-Signature"))) {
                    return new TestServer.Reply(200, "{\"code\":1,\"error\":\"bad signature\"}");
                }
                return new TestServer.Reply(200, TestServer.ok("{\"id\":\"tx-" + externalId + "\",\"external_id\":\"" + externalId + "\"}"));
            } catch (java.io.IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        })) {
            TronzapClient client = server.client();
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            try {
                List<Future<?>> futures = new ArrayList<>();
                for (int t = 0; t < threads; t++) {
                    int thread = t;
                    futures.add(pool.submit(() -> {
                        for (int i = 0; i < perThread; i++) {
                            String externalId = "order-" + thread + "-" + i;
                            Transaction tx = client.createEnergyTransaction(
                                    EnergyTransactionRequest.builder("TAddress", 65000).externalId(externalId).build());
                            assertEquals("tx-" + externalId, tx.id());
                            assertEquals(externalId, tx.externalId().orElseThrow());
                        }
                        return null;
                    }));
                }
                for (Future<?> future : futures) {
                    future.get();
                }
            } finally {
                pool.shutdownNow();
            }
            assertEquals(threads * perThread, server.requests().size());
        }
    }
}
