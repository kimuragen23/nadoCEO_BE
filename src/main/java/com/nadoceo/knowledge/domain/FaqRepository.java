package com.nadoceo.knowledge.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FaqRepository {

    Faq save(Faq faq);

    Optional<Faq> findById(UUID faqId);

    List<Faq> findByCourseIdOrderByUpvotesDesc(UUID courseId);
}
