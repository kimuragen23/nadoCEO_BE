package com.nadoceo.identity.domain;

public enum UserRole {
    STUDENT, INSTRUCTOR, ADMIN;

    public static UserRole from(String value) {
        if (value == null) return STUDENT;
        return switch (value.toLowerCase()) {
            case "instructor" -> INSTRUCTOR;
            case "admin" -> ADMIN;
            default -> STUDENT;
        };
    }
}
