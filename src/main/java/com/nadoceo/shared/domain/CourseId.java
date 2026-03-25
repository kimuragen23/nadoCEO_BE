package com.nadoceo.shared.domain;

import java.util.Objects;
import java.util.UUID;

public record CourseId(UUID value) {
    public CourseId {
        Objects.requireNonNull(value, "CourseId must not be null");
    }

    public static CourseId of(UUID value) {
        return new CourseId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
