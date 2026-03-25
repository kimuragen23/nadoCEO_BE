package com.nadoceo.coaching.domain;

public enum ChatType {
    MAIN, SUB;

    public static ChatType from(String value) {
        if (value == null || value.isBlank()) return MAIN;
        return switch (value.toLowerCase()) {
            case "sub" -> SUB;
            default -> MAIN;
        };
    }
}
