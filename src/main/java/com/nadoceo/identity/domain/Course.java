package com.nadoceo.identity.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    protected Course() {}

    public Course(UUID academyId, String name, String description) {
        this.academyId = academyId;
        this.name = name;
        this.description = description;
    }

    public UUID getId() { return id; }
    public UUID getAcademyId() { return academyId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
