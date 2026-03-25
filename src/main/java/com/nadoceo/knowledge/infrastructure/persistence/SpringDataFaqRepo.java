package com.nadoceo.knowledge.infrastructure.persistence;

import com.nadoceo.knowledge.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataFaqRepo extends JpaRepository<Faq, UUID> {

    List<Faq> findByCourseIdOrderByUpvotesDesc(UUID courseId);
}
