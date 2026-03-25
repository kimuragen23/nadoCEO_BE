package com.nadoceo.identity.presentation.dto;

import com.nadoceo.identity.domain.Course;

import java.util.UUID;

public record CourseResponse(
        UUID id,
        String name,
        String description
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getId(), course.getName(), course.getDescription());
    }
}
