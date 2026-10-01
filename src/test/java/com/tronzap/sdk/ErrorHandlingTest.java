package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tronzap.sdk.exception.ApiErrorCode;
import com.tronzap.sdk.exception.ApiException;
import com.tronzap.sdk.exception.HttpException;
import com.tronzap.sdk.exception.InvalidResponseException;
import com.tronzap.sdk.exception.RateLimitException;
import com.tronzap.sdk.exception.ServerException;
import com.tronzap.sdk.exception.TronzapException;
import com.tronzap.sdk.exception.UnauthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ErrorHandlingTest {

    private static TronzapException failure(int status, String body) throws IOException {
        try (TestServer server = TestServer.start().reply(status, body)) {
            return assertThrows(TronzapException.class, () -> server.client().getBalance());
        }
    }

    @Test
    void apiErrorWithSuccessStatusIsNotTreatedAsSuccess() throws Exception {
        String body = "{\"code\":6,\"key\":\"insufficient_funds\",\"request_id\":\"req-9\",\"error\":\"Insufficient funds\"}";

        ApiException e = assertInstanceOf(ApiException.class, failure(200, body));

        assertEquals(6, e.getCode());
        assertSame(ApiErrorCode.INSUFFICIENT_FUNDS, e.getErrorCode());
        assertEquals("Insufficient funds", e.getMessage());
        assertEquals("insufficient_funds", e.getErrorKey().orElseThrow());
        assertEquals("req-9", e.getRequestId().orElseThrow());
        assertEquals(200, e.getStatusCode());
        assertEquals(body, e.getResponseBody());
    }

    @Test
    void apiErrorWinsOverClientErrorStatus() throws Exception {
        ApiException e = assertInstanceOf(ApiException.class,
                failure(400, "{\"code\":10,\"key\":\"invalid_tron_address.from_address\",\"error\":\"Invalid TRON address\"}"));

        assertSame(ApiErrorCode.INVALID_TRON_ADDRESS, e.getErrorCode());
        assertEquals("invalid_tron_address.from_address", e.getErrorKey().orElseThrow());
        assertEquals(400, e.getStatusCode());
        assertTrue(e.getRequestId().isEmpty());
    }

    @Test
    void apiErrorWinsOverServerErrorStatus() throws Exception {
        ApiException e = assertInstanceOf(ApiException.class,
                failure(500, "{\"code\":500,\"key\":\"internal_server_error\",\"error\":\"Internal server error\"}"));

        assertSame(ApiErrorCode.INTERNAL_SERVER_ERROR, e.getErrorCode());
        assertEquals(500, e.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 401})
    void authenticationErrorFromApi(int status) throws Exception {
        ApiException e = assertInstanceOf(ApiException.class,
                failure(status, "{\"code\":1,\"key\":\"auth_error\",\"error\":\"Authentication error\"}"));

        assertSame(ApiErrorCode.AUTH_ERROR, e.getErrorCode());
        assertEquals(status, e.getStatusCode());
    }

    @Test
    void transactionNotFoundUsesStableConstantName() throws Exception {
        ApiException e = assertInstanceOf(ApiException.class,
                failure(404, "{\"code\":20,\"key\":\"subscription_not_found\",\"error\":\"Not found\"}"));

        assertSame(ApiErrorCode.TRANSACTION_NOT_FOUND, e.getErrorCode());
        assertEquals("subscription_not_found", e.getErrorKey().orElseThrow());
    }

    @Test
    void missingCodeIsAnApiError() throws Exception {
        ApiException e = assertInstanceOf(ApiException.class, failure(200, "{\"result\":{\"balance\":1}}"));

        assertEquals(1, e.getCode());
        assertEquals("Unknown API error", e.getMessage());
    }

    @Test
    void codeAsStringIsUnderstood() throws Exception {
        ApiException e = assertInstanceOf(ApiException.class, failure(200, "{\"code\":\"25\",\"error\":\"Already\"}"));

        assertSame(ApiErrorCode.ADDRESS_ALREADY_ACTIVATED, e.getErrorCode());
    }

    @Test
    void unknownCodeKeepsTheNumber() throws Exception {
        ApiException e = assertInstanceOf(ApiException.class, failure(200, "{\"code\":777,\"error\":\"New failure\"}"));

        assertEquals(777, e.getCode());
        assertSame(ApiErrorCode.UNKNOWN, e.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "\"text\"", "42", "null", "true"})
    void validJsonThatIsNotAnObjectIsAnApiError(String body) throws Exception {
        ApiException e = assertInstanceOf(ApiException.class, failure(200, body));

        assertEquals(1, e.getCode());
        assertEquals(body, e.getResponseBody());
    }

    @Test
    void unauthorizedWithoutApiPayload() throws Exception {
        UnauthorizedException e = assertInstanceOf(UnauthorizedException.class, failure(401, "<html>401</html>"));

        assertEquals(401, e.getStatusCode());
        assertEquals("<html>401</html>", e.getResponseBody());
    }

    @Test
    void forbiddenWithoutApiPayload() throws Exception {
        assertEquals(403, assertInstanceOf(UnauthorizedException.class, failure(403, "Forbidden")).getStatusCode());
    }

    @Test
    void rateLimited() throws Exception {
        assertEquals(429, assertInstanceOf(RateLimitException.class, failure(429, "slow down")).getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    void serverError(int status) throws Exception {
        ServerException e = assertInstanceOf(ServerException.class, failure(status, "<html>bad gateway</html>"));

        assertEquals(status, e.getStatusCode());
    }

    @Test
    void serverErrorWithBrokenJson() throws Exception {
        assertInstanceOf(ServerException.class, failure(500, "{\"code\":"));
    }

    @ParameterizedTest
    @CsvSource({"404,Not Found", "400,Bad Request", "302,Moved"})
    void otherHttpErrorsUseTheBaseClass(int status, String body) throws Exception {
        TronzapException e = failure(status, body);

        assertSame(HttpException.class, e.getClass());
        assertEquals(status, ((HttpException) e).getStatusCode());
    }

    @Test
    void httpErrorWithApiSuccessPayloadIsStillAnHttpError() throws Exception {
        assertInstanceOf(ServerException.class, failure(503, "{\"code\":0,\"result\":{}}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"code\":0,\"result\":{", "not json", "<html>ok</html>", "{\"code\":0,\"result\":{}} trailing"})
    void malformedJsonOnSuccess(String body) throws Exception {
        InvalidResponseException e = assertInstanceOf(InvalidResponseException.class, failure(200, body));

        assertEquals(200, e.getStatusCode());
        assertEquals(body, e.getResponseBody());
    }

    @Test
    void emptyBodyOnSuccess() throws Exception {
        assertInstanceOf(InvalidResponseException.class, failure(200, ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"code\":0}", "{\"code\":0,\"result\":null}"})
    void missingResult(String body) throws Exception {
        InvalidResponseException e = assertInstanceOf(InvalidResponseException.class, failure(200, body));

        assertTrue(e.getMessage().contains("missing result"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"code\":0,\"result\":{\"balance\":{\"nested\":true}}}",
        "{\"code\":0,\"result\":{\"balance\":\"lots\"}}",
        "{\"code\":0,\"result\":\"balance\"}",
        "{\"code\":0,\"result\":[1,2]}"
    })
    void resultOfTheWrongShape(String body) throws Exception {
        InvalidResponseException e = assertInstanceOf(InvalidResponseException.class, failure(200, body));

        assertTrue(e.getMessage().startsWith("unexpected result"), e.getMessage());
        assertEquals(body, e.getResponseBody());
    }

    @Test
    void integerOutOfRange() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{\"page\":99999999999}")) {
            assertThrows(InvalidResponseException.class, () -> server.client().getAmlHistory());
        }
    }

    @Test
    void fractionalIntegerIsRejected() throws Exception {
        try (TestServer server = TestServer.start().replyOk("{\"resources\":{\"energy\":1.5}}")) {
            assertThrows(InvalidResponseException.class, () -> server.client().getAddressInfo("TAddress"));
        }
    }

    @Test
    void wrongShapeOfNestedCollections() throws Exception {
        try (TestServer server = TestServer.start()) {
            TronzapClient client = server.client();

            server.replyOk("{\"energy\":{\"a\":1}}");
            assertThrows(InvalidResponseException.class, client::getServices);
            server.replyOk("{\"items\":[\"not an object\"]}");
            assertThrows(InvalidResponseException.class, client::getAmlHistory);
            server.replyOk("{\"id\":\"a\",\"checked_at\":{\"date\":1}}");
            assertThrows(InvalidResponseException.class, () -> client.checkAmlStatus("a"));
            server.replyOk("{\"id\":\"a\",\"blacklist\":\"maybe\"}");
            assertThrows(InvalidResponseException.class, () -> client.checkAmlStatus("a"));
            server.replyOk("{\"id\":{\"a\":1}}");
            assertThrows(InvalidResponseException.class, () -> client.checkAmlStatus("a"));
            server.replyOk("{\"balances\":{\"TRX\":[1]}}");
            assertThrows(InvalidResponseException.class, () -> client.getAddressInfo("TAddress"));
        }
    }

    @Test
    void responseLargerThanTheLimitIsRejected() throws Exception {
        try (TestServer server = TestServer.start().handler((exchange, request) -> {
            exchange.sendResponseHeaders(200, 0);
            byte[] chunk = new byte[64 * 1024];
            try (OutputStream out = exchange.getResponseBody()) {
                for (long sent = 0; sent <= TronzapClient.MAX_RESPONSE_BYTES; sent += chunk.length) {
                    out.write(chunk);
                }
            } catch (IOException ignored) {
            }
        })) {
            InvalidResponseException e = assertThrows(InvalidResponseException.class, () -> server.client().getBalance());

            assertTrue(e.getMessage().contains("exceeds"), e.getMessage());
            assertEquals(200, e.getStatusCode());
        }
    }
}
