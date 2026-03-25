package com.nadoceo.analytics.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_path_events")
public class LearningPathEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "faq_hit_id")
    private UUID faqHitId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected LearningPathEvent() {}

    public LearningPathEvent(UUID sessionId, UUID studentId, UUID courseId,
                              EventType eventType, int turnNumber, String content,
                              UUID faqHitId, String metadata) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.eventType = eventType;
        this.turnNumber = turnNumber;
        this.content = content;
        this.faqHitId = faqHitId;
        this.metadata = metadata;
    }

    public UUID getId() { return id; }
    public UUID getSessionId() { return sessionId; }
    public UUID getStudentId() { return studentId; }
    public UUID getCourseId() { return courseId; }
    public EventType getEventType() { return eventType; }
    public int getTurnNumber() { return turnNumber; }
    public String getContent() { return content; }
    public UUID getFaqHitId() { return faqHitId; }
    public String getMetadata() { return metadata; }
    public Instant getCreatedAt() { return createdAt; }
}
