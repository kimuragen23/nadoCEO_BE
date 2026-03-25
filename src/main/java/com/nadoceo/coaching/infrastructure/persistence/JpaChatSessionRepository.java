package com.nadoceo.coaching.infrastructure.persistence;

import com.nadoceo.coaching.domain.ChatSession;
import com.nadoceo.coaching.domain.ChatSessionRepository;
import com.nadoceo.coaching.domain.ChatType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaChatSessionRepository implements ChatSessionRepository {

    private final SpringDataChatSessionRepo springDataRepo;

    public JpaChatSessionRepository(SpringDataChatSessionRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public ChatSession save(ChatSession session) {
        return springDataRepo.save(session);
    }

    @Override
    public Optional<ChatSession> findById(UUID sessionId) {
        return springDataRepo.findById(sessionId);
    }

    @Override
    public List<ChatSession> findByStudentId(UUID studentId) {
        return springDataRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    public List<ChatSession> findByStudentIdAndChatType(UUID studentId, ChatType chatType) {
        return springDataRepo.findByStudentIdAndChatTypeOrderByCreatedAtDesc(studentId, chatType);
    }

    @Override
    public List<ChatSession> findByCourseIdAndResolved(UUID courseId) {
        return springDataRepo.findByCourseIdAndResolvedTrue(courseId);
    }
}
