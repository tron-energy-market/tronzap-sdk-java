package com.tronzap.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tronzap.sdk.exception.ApiException;
import com.tronzap.sdk.exception.HttpException;
import com.tronzap.sdk.exception.InvalidResponseException;
import com.tronzap.sdk.exception.RateLimitException;
import com.tronzap.sdk.exception.ServerException;
import com.tronzap.sdk.exception.UnauthorizedException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class JsonCodec {

    private static final String UNKNOWN_API_ERROR = "Unknown API error";

    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
            .build();

    ObjectNode object() {
        return mapper.createObjectNode();
    }

    byte[] write(ObjectNode params) {
        try {
            return mapper.writeValueAsBytes(params);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cannot encode request parameters", e);
        }
    }

    /**
     * The API reports some failures with a 2xx status and others with 4xx/5xx, so a payload with a
     * non-zero code must win over the HTTP status.
     */
    JsonNode decode(int statusCode, byte[] raw) {
        String body = new String(raw, StandardCharsets.UTF_8);
        JsonNode root = null;
        IOException parseError = null;
        try {
            root = mapper.readTree(raw);
            if (root != null && root.isMissingNode()) {
                root = null;
            }
        } catch (IOException e) {
            parseError = e;
        }

        if (root != null) {
            if (!root.isObject()) {
                throw new ApiException(UNKNOWN_API_ERROR, 1, null, null, statusCode, body);
            }
            Integer code = code(root.get("code"));
            if (code == null || code != 0) {
                throw new ApiException(
                        textOr(root.get("error"), UNKNOWN_API_ERROR),
                        code == null ? 1 : code,
                        textOr(root.get("key"), null),
                        textOr(root.get("request_id"), null),
                        statusCode,
                        body);
            }
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw httpError(statusCode, body);
        }
        if (root == null) {
            throw new InvalidResponseException("invalid JSON response", statusCode, body, parseError);
        }
        JsonNode result = root.get("result");
        if (result == null || result.isNull()) {
            throw new InvalidResponseException("missing result in response", statusCode, body, null);
        }
        return result;
    }

    private static Integer code(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isIntegralNumber() && node.canConvertToInt()) {
            return node.intValue();
        }
        if (node.isTextual()) {
            try {
                return Integer.valueOf(node.textValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String textOr(JsonNode node, String fallback) {
        if (node == null || !node.isValueNode() || node.isNull()) {
            return fallback;
        }
        String text = node.asText();
        return text.isBlank() ? fallback : text;
    }

    private static HttpException httpError(int statusCode, String body) {
        if (statusCode == 429) {
            return new RateLimitException("too many requests", statusCode, body);
        }
        if (statusCode == 401 || statusCode == 403) {
            return new UnauthorizedException("unauthorized", statusCode, body);
        }
        if (statusCode >= 500) {
            return new ServerException("server error", statusCode, body);
        }
        return new HttpException("HTTP error " + statusCode, statusCode, body);
    }
}
