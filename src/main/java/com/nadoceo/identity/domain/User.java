package com.nadoceo.identity.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private String name;

    protected User() {}

    public User(UUID academyId, UserRole role, String name) {
        this.academyId = academyId;
        this.role = role;
        this.name = name;
    }

    public UUID getId() { return id; }
    public UUID getAcademyId() { return academyId; }
    public UserRole getRole() { return role; }
    public String getName() { return name; }

    public boolean isInstructor() { return role == UserRole.INSTRUCTOR; }
    public boolean isStudent() { return role == UserRole.STUDENT; }
}
