package com.nadoceo.coaching.domain;

import com.nadoceo.coaching.domain.event.SessionResolved;
import com.nadoceo.coaching.domain.event.StudentQuestionAsked;
import com.nadoceo.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
public class ChatSession extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type", nullable = false, length = 10)
    private ChatType chatType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String messages = "[]";

    @Column(name = "total_turns")
    private int totalTurns = 0;

    @Column
    private boolean resolved = false;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "faq_id")
    private UUID faqId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ChatSession() {}

    public ChatSession(UUID studentId, UUID courseId, ChatType chatType) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.chatType = chatType;
    }

    // --- Rich Domain Methods ---

    public int advanceTurn() {
        this.totalTurns++;
        return this.totalTurns;
    }

    public void recordQuestion(String message) {
        int turn = advanceTurn();
        registerEvent(new StudentQuestionAsked(id, studentId, courseId, turn, message));
    }

    /**
     * 대화 히스토리에 메시지를 추가한다.
     * messages JSON: [{"role":"user","content":"..."},{"role":"assistant","content":"..."}]
     */
    public void appendMessage(String role, String content) {
        // 간단한 JSON 배열 추가 (jackson 없이)
        String escaped = content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        String entry = "{\"role\":\"" + role + "\",\"content\":\"" + escaped + "\"}";
        if (messages == null || messages.equals("[]")) {
            messages = "[" + entry + "]";
        } else {
            messages = messages.substring(0, messages.length() - 1) + "," + entry + "]";
        }
    }

    public void markResolved(String summary) {
        this.resolved = true;
        this.resolvedAt = Instant.now();
        registerEvent(new SessionResolved(id, studentId, courseId, totalTurns, summary));
    }

    public void linkFaq(UUID faqId) {
        this.faqId = faqId;
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getCourseId() { return courseId; }
    public ChatType getChatType() { return chatType; }
    public String getMessages() { return messages; }
    public int getTotalTurns() { return totalTurns; }
    public boolean isResolved() { return resolved; }
    public Instant getResolvedAt() { return resolvedAt; }
    public UUID getFaqId() { return faqId; }
    public Instant getCreatedAt() { return createdAt; }

    public void setMessages(String messages) { this.messages = messages; }
}
