package com.nadoceo.identity.presentation;

import com.nadoceo.identity.domain.User;
import com.nadoceo.identity.infrastructure.persistence.SpringDataCourseRepo;
import com.nadoceo.identity.infrastructure.persistence.SpringDataUserRepo;
import com.nadoceo.identity.presentation.dto.CourseResponse;
import com.nadoceo.identity.presentation.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Identity", description = "사용자/과목 관리 API")
@RestController
@RequestMapping("/api/v1")
public class IdentityController {

    private final SpringDataUserRepo userRepo;
    private final SpringDataCourseRepo courseRepo;

    public IdentityController(SpringDataUserRepo userRepo, SpringDataCourseRepo courseRepo) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
    }

    @Operation(summary = "사용자 프로필 조회")
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @Operation(summary = "과목 목록 조회")
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getCourses() {
        var courses = courseRepo.findAll().stream().map(CourseResponse::from).toList();
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "학원별 과목 목록 조회")
    @GetMapping("/courses/academy/{academyId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByAcademy(@PathVariable UUID academyId) {
        var courses = courseRepo.findByAcademyId(academyId).stream().map(CourseResponse::from).toList();
        return ResponseEntity.ok(courses);
    }
}
