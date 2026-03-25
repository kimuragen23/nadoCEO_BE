package com.nadoceo.shared.domain;

import java.util.Objects;
import java.util.UUID;

public record StudentId(UUID value) {
    public StudentId {
        Objects.requireNonNull(value, "StudentId must not be null");
    }

    public static StudentId of(UUID value) {
        return new StudentId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
