package com.nadoceo.identity.infrastructure.persistence;

import com.nadoceo.identity.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataCourseRepo extends JpaRepository<Course, UUID> {
    List<Course> findByAcademyId(UUID academyId);
}
