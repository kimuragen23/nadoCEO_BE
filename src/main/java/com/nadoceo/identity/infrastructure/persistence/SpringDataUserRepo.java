package com.nadoceo.identity.infrastructure.persistence;

import com.nadoceo.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataUserRepo extends JpaRepository<User, UUID> {
}
