package com.tronzap.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.http.HttpClient;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TronzapClientBuilderTest {

    private static TronzapClientBuilder configured() {
        return TronzapClient.builder().apiToken("token").apiSecret("secret");
    }

    @Test
    void buildsWithDefaults() {
        assertNotNull(configured().build());
    }

    @Test
    void requiresToken() {
        assertThrows(IllegalStateException.class, () -> TronzapClient.builder().apiSecret("secret").build());
        assertThrows(IllegalStateException.class, () -> TronzapClient.builder().apiToken(" ").apiSecret("secret").build());
        assertThrows(NullPointerException.class, () -> TronzapClient.builder().apiToken(null));
    }

    @Test
    void requiresSecret() {
        assertThrows(IllegalStateException.class, () -> TronzapClient.builder().apiToken("token").build());
        assertThrows(IllegalStateException.class, () -> TronzapClient.builder().apiToken("token").apiSecret("").build());
        assertThrows(NullPointerException.class, () -> TronzapClient.builder().apiSecret(null));
    }

    @Test
    void rejectsNonPositiveTimeouts() {
        assertThrows(IllegalArgumentException.class, () -> configured().timeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> configured().timeout(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> configured().connectTimeout(Duration.ZERO));
        assertThrows(NullPointerException.class, () -> configured().timeout(null));
    }

    @Test
    void connectTimeoutConflictsWithCustomHttpClient() {
        TronzapClientBuilder builder = configured()
                .httpClient(HttpClient.newHttpClient())
                .connectTimeout(Duration.ofSeconds(5));

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void connectTimeoutWithOwnClient() {
        assertNotNull(configured().connectTimeout(Duration.ofSeconds(5)).timeout(Duration.ofSeconds(20)).build());
    }

    @Test
    void rejectsBlankUserAgent() {
        assertThrows(IllegalArgumentException.class, () -> configured().userAgent(" "));
        assertThrows(NullPointerException.class, () -> configured().httpClient(null));
    }

    @ParameterizedTest
    @CsvSource({
        "https://api.tronzap.com, https://api.tronzap.com",
        "api.tronzap.com, https://api.tronzap.com",
        "api.tronzap.com/, https://api.tronzap.com",
        "//api.tronzap.com, https://api.tronzap.com",
        "'  https://api.tronzap.com//  ', https://api.tronzap.com",
        "http://localhost:8080, http://localhost:8080",
        "https://dev.example.com/api/, https://dev.example.com/api"
    })
    void normalizesBaseUrl(String input, String expected) {
        assertEquals(expected, TronzapClientBuilder.normalizeBaseUrl(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "/", "ftp://api.tronzap.com", "https://", "https://api.tronzap.com?x=1", "https://api tronzap.com"})
    void rejectsInvalidBaseUrl(String input) {
        assertThrows(IllegalArgumentException.class, () -> configured().baseUrl(input).build());
    }

    @Test
    void versionMatchesTheBuild() {
        String projectVersion = System.getProperty("tronzap.project.version");
        if (projectVersion != null) {
            assertEquals(projectVersion, TronzapClient.VERSION);
        }
    }
}
