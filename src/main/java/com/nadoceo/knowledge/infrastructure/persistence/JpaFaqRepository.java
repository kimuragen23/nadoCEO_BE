package com.nadoceo.knowledge.infrastructure.persistence;

import com.nadoceo.knowledge.domain.Faq;
import com.nadoceo.knowledge.domain.FaqRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaFaqRepository implements FaqRepository {

    private final SpringDataFaqRepo springDataRepo;

    public JpaFaqRepository(SpringDataFaqRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Faq save(Faq faq) {
        return springDataRepo.save(faq);
    }

    @Override
    public Optional<Faq> findById(UUID faqId) {
        return springDataRepo.findById(faqId);
    }

    @Override
    public List<Faq> findByCourseIdOrderByUpvotesDesc(UUID courseId) {
        return springDataRepo.findByCourseIdOrderByUpvotesDesc(courseId);
    }
}
