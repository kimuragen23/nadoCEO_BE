package com.nadoceo.shared.domain;

import java.util.Objects;
import java.util.UUID;

public record FaqId(UUID value) {
    public FaqId {
        Objects.requireNonNull(value, "FaqId must not be null");
    }

    public static FaqId of(UUID value) {
        return new FaqId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
