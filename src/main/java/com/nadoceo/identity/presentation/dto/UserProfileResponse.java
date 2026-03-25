package com.nadoceo.identity.presentation.dto;

import com.nadoceo.identity.domain.User;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String role
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getId(), user.getName(), user.getRole().name().toLowerCase());
    }
}
