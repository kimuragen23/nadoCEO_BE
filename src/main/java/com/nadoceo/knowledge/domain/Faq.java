package com.nadoceo.knowledge.domain;

import com.nadoceo.shared.domain.AggregateRoot;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "faqs")
public class Faq extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column
    private int upvotes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FaqSource source = FaqSource.STUDENT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Faq() {}

    public Faq(UUID courseId, String question, String answer, FaqSource source) {
        this.courseId = courseId;
        this.question = question;
        this.answer = answer;
        this.source = source;
    }

    // --- Rich Domain Methods ---

    public void upvote() {
        this.upvotes++;
    }

    public boolean isPublicReady() {
        return this.upvotes >= 3;
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getUpvotes() { return upvotes; }
    public FaqSource getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }
}
