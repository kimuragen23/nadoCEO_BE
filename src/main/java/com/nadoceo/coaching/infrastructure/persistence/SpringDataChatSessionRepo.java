package com.nadoceo.coaching.infrastructure.persistence;

import com.nadoceo.coaching.domain.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataChatSessionRepo extends JpaRepository<ChatSession, UUID> {

    List<ChatSession> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<ChatSession> findByCourseIdAndResolvedTrue(UUID courseId);
}
