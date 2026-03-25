package com.nadoceo.knowledge.domain;

public enum FaqSource {
    STUDENT, INSTRUCTOR;

    public static FaqSource from(String value) {
        if (value == null || value.isBlank()) return INSTRUCTOR;
        return switch (value.toLowerCase()) {
            case "student" -> STUDENT;
            default -> INSTRUCTOR;
        };
    }
}
