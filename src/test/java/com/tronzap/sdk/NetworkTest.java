package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tronzap.sdk.exception.ConnectionException;
import com.tronzap.sdk.exception.NetworkException;
import com.tronzap.sdk.exception.RequestInterruptedException;
import com.tronzap.sdk.exception.SslException;
import com.tronzap.sdk.exception.RequestTimeoutException;
import com.tronzap.sdk.response.Balance;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class NetworkTest {

    private static final char[] KEYSTORE_PASSWORD = "test-only".toCharArray();

    private static KeyStore selfSigned;

    @BeforeAll
    static void createSelfSignedCertificate(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("server.p12");
        String keytool = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool").toString();
        Process process = new ProcessBuilder(keytool, "-genkeypair",
                "-alias", "server", "-keyalg", "EC", "-groupname", "secp256r1", "-validity", "1",
                "-dname", "CN=localhost", "-ext", "SAN=ip:127.0.0.1,dns:localhost",
                "-storetype", "PKCS12", "-keystore", file.toString(),
                "-storepass", new String(KEYSTORE_PASSWORD), "-keypass", new String(KEYSTORE_PASSWORD), "-noprompt")
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), output);
        selfSigned = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(file)) {
            selfSigned.load(in, KEYSTORE_PASSWORD);
        }
    }

    private static SSLContext serverContext() throws Exception {
        KeyManagerFactory keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keys.init(selfSigned, KEYSTORE_PASSWORD);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keys.getKeyManagers(), null, null);
        return context;
    }

    private static SSLContext trustingContext() throws Exception {
        TrustManagerFactory trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(selfSigned);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trust.getTrustManagers(), null);
        return context;
    }

    @Test
    void untrustedCertificateIsAnSslError() throws Exception {
        try (TestServer server = TestServer.startTls(serverContext()).replyOk("{}")) {
            SslException e = assertThrows(SslException.class, () -> server.client().getBalance());

            assertNotNull(e.getCause());
            assertTrue(server.requests().isEmpty(), "no request may reach an untrusted server");
        }
    }

    @Test
    void customHttpClientCanTrustTheCertificate() throws Exception {
        try (TestServer server = TestServer.startTls(serverContext()).replyOk("{\"balance\":1,\"address\":\"T1\"}")) {
            HttpClient trusting = HttpClient.newBuilder().sslContext(trustingContext()).build();

            Balance balance = server.clientBuilder().httpClient(trusting).build().getBalance();

            assertEquals(0, BigDecimal.ONE.compareTo(balance.balance()));
        }
    }

    @Test
    void customHttpClientIsUsed() throws Exception {
        AtomicInteger tasks = new AtomicInteger();
        HttpClient counting = HttpClient.newBuilder()
                .executor(task -> {
                    tasks.incrementAndGet();
                    new Thread(task).start();
                })
                .build();
        try (TestServer server = TestServer.start().replyOk("{}")) {
            server.clientBuilder().httpClient(counting).build().getBalance();
        }
        assertTrue(tasks.get() > 0);
    }

    @Test
    void slowResponseTimesOut() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        try (TestServer server = TestServer.start().handler((exchange, request) -> {
            await(release);
            TestServer.write(exchange, 200, TestServer.ok("{}"));
        })) {
            TronzapClient client = server.clientBuilder().timeout(Duration.ofMillis(300)).build();

            long started = System.nanoTime();
            RequestTimeoutException e = assertThrows(RequestTimeoutException.class, client::getBalance);
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            release.countDown();

            assertTrue(elapsedMs < 5000, "timed out after " + elapsedMs + " ms");
            assertNotNull(e.getCause());
        }
    }

    @Test
    void slowBodyAfterHeadersTimesOut() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        try (TestServer server = TestServer.start().handler((exchange, request) -> {
            exchange.sendResponseHeaders(200, 0);
            OutputStream out = exchange.getResponseBody();
            out.write("{\"code\":0,".getBytes(StandardCharsets.UTF_8));
            out.flush();
            await(release);
            out.close();
        })) {
            TronzapClient client = server.clientBuilder().timeout(Duration.ofMillis(300)).build();

            assertThrows(RequestTimeoutException.class, client::getBalance);
            release.countDown();
        }
    }

    @Test
    void refusedConnection() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = socket.getLocalPort();
        }
        TronzapClient client = TronzapClient.builder()
                .apiToken(TestServer.TOKEN)
                .apiSecret(TestServer.SECRET)
                .baseUrl("http://127.0.0.1:" + port)
                .build();

        ConnectionException e = assertThrows(ConnectionException.class, client::getBalance);

        assertNotNull(e.getCause());
    }

    @Test
    void connectionClosedWithoutResponse() throws Exception {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            Thread acceptor = new Thread(() -> {
                try (Socket accepted = socket.accept()) {
                    accepted.getInputStream().read(new byte[1024]);
                } catch (IOException ignored) {
                }
            });
            acceptor.start();
            TronzapClient client = TronzapClient.builder()
                    .apiToken(TestServer.TOKEN)
                    .apiSecret(TestServer.SECRET)
                    .baseUrl("http://127.0.0.1:" + socket.getLocalPort())
                    .build();

            NetworkException e = assertThrows(NetworkException.class, client::getBalance);

            assertFalse(e instanceof RequestTimeoutException, "a dropped connection is not a timeout");
            acceptor.join(5000);
        }
    }

    @Test
    void interruptionIsNotReportedAsTimeout() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        try (TestServer server = TestServer.start().handler((exchange, request) -> {
            await(release);
            TestServer.write(exchange, 200, TestServer.ok("{}"));
        })) {
            TronzapClient client = server.client();
            Thread.currentThread().interrupt();
            try {
                RequestInterruptedException e = assertThrows(RequestInterruptedException.class, client::getBalance);

                assertInstanceOf(InterruptedException.class, e.getCause());
                assertTrue(Thread.currentThread().isInterrupted(), "interrupt status must be restored");
            } finally {
                Thread.interrupted();
                release.countDown();
            }
        }
    }

    @Test
    void classifiesUnknownTransportFailure() {
        NetworkException e = NetworkErrors.classify(new IOException());

        assertEquals(NetworkException.class, e.getClass());
        assertTrue(e.getMessage().contains("java.io.IOException"));
    }

    @Test
    void classifiesByCauseChain() {
        assertInstanceOf(SslException.class, NetworkErrors.classify(new IOException("wrapped", new javax.net.ssl.SSLHandshakeException("bad cert"))));
        assertInstanceOf(RequestTimeoutException.class, NetworkErrors.classify(new java.net.SocketTimeoutException("read timed out")));
        assertInstanceOf(ConnectionException.class, NetworkErrors.classify(new java.net.UnknownHostException("api.example.invalid")));
        assertInstanceOf(ConnectionException.class, NetworkErrors.classify(new IOException(new java.nio.channels.UnresolvedAddressException())));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
