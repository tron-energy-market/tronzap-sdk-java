package com.tronzap.sdk.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A timestamp as the API sent it, together with its parsed value.
 *
 * <p>The API encodes timestamps in several ways: RFC 3339 with an offset ({@code
 * 2026-08-07T10:42:12+00:00} or {@code ...Z}), a date-time without an offset, a space-separated
 * date-time, a bare date or Unix seconds. Values without an offset are read as UTC. A value that
 * matches none of these keeps its {@link #raw()} text and has an empty {@link #value()}, so an
 * unexpected format never fails the whole response.
 *
 * @param raw the timestamp exactly as the API sent it
 * @param value the parsed timestamp, empty when {@code raw} could not be parsed
 */
public record Timestamp(String raw, Optional<OffsetDateTime> value) {

    private static final DateTimeFormatter SPACE_SEPARATED = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter();

    private static final List<Function<String, OffsetDateTime>> PARSERS = List.of(
            OffsetDateTime::parse,
            text -> LocalDateTime.parse(text).atOffset(ZoneOffset.UTC),
            text -> LocalDateTime.parse(text, SPACE_SEPARATED).atOffset(ZoneOffset.UTC),
            text -> LocalDate.parse(text).atStartOfDay().atOffset(ZoneOffset.UTC));

    /**
     * Validates the components.
     *
     * @param raw the timestamp exactly as the API sent it
     * @param value the parsed timestamp, empty when {@code raw} could not be parsed
     */
    public Timestamp {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(value, "value");
    }

    /**
     * Parses a timestamp in any of the encodings the API uses.
     *
     * @param raw the timestamp text
     * @return the timestamp; its {@link #value()} is empty when the text matches no known encoding
     */
    public static Timestamp parse(String raw) {
        Objects.requireNonNull(raw, "raw");
        return new Timestamp(raw, Optional.ofNullable(tryParse(raw.trim())));
    }

    private static OffsetDateTime tryParse(String text) {
        if (text.isEmpty()) {
            return null;
        }
        for (Function<String, OffsetDateTime> parser : PARSERS) {
            try {
                return parser.apply(text);
            } catch (DateTimeParseException ignored) {
            }
        }
        if (text.length() <= 12 && text.chars().allMatch(Character::isDigit)) {
            return Instant.ofEpochSecond(Long.parseLong(text)).atOffset(ZoneOffset.UTC);
        }
        return null;
    }
}
