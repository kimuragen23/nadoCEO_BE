package com.nadoceo.knowledge.application;

import com.nadoceo.knowledge.domain.Faq;
import com.nadoceo.knowledge.domain.FaqRepository;
import com.nadoceo.knowledge.domain.FaqSource;
import com.nadoceo.knowledge.domain.VectorSearchPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class CreateFaqUseCase {

    private final FaqRepository faqRepository;
    private final VectorSearchPort vectorSearch;

    public CreateFaqUseCase(FaqRepository faqRepository, VectorSearchPort vectorSearch) {
        this.faqRepository = faqRepository;
        this.vectorSearch = vectorSearch;
    }

    public record Command(UUID courseId, String question, String answer, String source) {}

    public Faq execute(Command command) {
        Faq faq = new Faq(
                command.courseId(), command.question(), command.answer(),
                FaqSource.from(command.source()));
        faq = faqRepository.save(faq);

        // 벡터 저장소에 임베딩
        vectorSearch.store(
                faq.getId().toString(),
                faq.getQuestion(),
                Map.of(
                        "faqId", faq.getId().toString(),
                        "courseId", faq.getCourseId().toString(),
                        "question", faq.getQuestion(),
                        "answer", faq.getAnswer()
                )
        );

        return faq;
    }
}
