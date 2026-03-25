package com.nadoceo.knowledge.application;

import com.nadoceo.knowledge.domain.FaqRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpvoteFaqUseCase {

    private final FaqRepository faqRepository;

    public UpvoteFaqUseCase(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public void execute(UUID faqId) {
        var faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("FAQ not found: " + faqId));
        faq.upvote();
        faqRepository.save(faq);
    }
}
