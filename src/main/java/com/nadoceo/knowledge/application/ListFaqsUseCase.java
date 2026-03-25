package com.nadoceo.knowledge.application;

import com.nadoceo.knowledge.domain.Faq;
import com.nadoceo.knowledge.domain.FaqRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListFaqsUseCase {

    private final FaqRepository faqRepository;

    public ListFaqsUseCase(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<Faq> execute(UUID courseId) {
        return faqRepository.findByCourseIdOrderByUpvotesDesc(courseId);
    }
}
