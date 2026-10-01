package com.tronzap.sdk.request;

import java.util.Objects;
import java.util.Optional;

final class Checks {

    private Checks() {
    }

    static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    static Optional<String> optional(Optional<String> value, String name) {
        Objects.requireNonNull(value, name);
        value.ifPresent(present -> required(present, name));
        return value;
    }

    static long positive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive, got " + value);
        }
        return value;
    }

    static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive, got " + value);
        }
        return value;
    }

    static <E extends Enum<E>> E known(E value, E unknown, String name) {
        if (value == null || value == unknown) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
