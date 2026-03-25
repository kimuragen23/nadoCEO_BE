package com.nadoceo.coaching.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ChatSession Repository Port — 도메인이 정의하는 인터페이스.
 */
public interface ChatSessionRepository {

    ChatSession save(ChatSession session);

    Optional<ChatSession> findById(UUID sessionId);

    List<ChatSession> findByStudentId(UUID studentId);

    List<ChatSession> findByStudentIdAndChatType(UUID studentId, ChatType chatType);

    List<ChatSession> findByCourseIdAndResolved(UUID courseId);
}
