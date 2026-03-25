package com.nadoceo.identity.domain;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User implements Persistable<UUID> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private String name;

    @Transient
    private boolean isNew = false;

    protected User() {}

    public User(UUID id, UUID academyId, UserRole role, String name) {
        this.id = id != null ? id : UUID.randomUUID();
        this.academyId = academyId;
        this.role = role;
        this.name = name;
        this.isNew = true;
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }

    public UUID getAcademyId() { return academyId; }
    public UserRole getRole() { return role; }
    public String getName() { return name; }

    public boolean isInstructor() { return role == UserRole.INSTRUCTOR; }
    public boolean isStudent() { return role == UserRole.STUDENT; }
}
