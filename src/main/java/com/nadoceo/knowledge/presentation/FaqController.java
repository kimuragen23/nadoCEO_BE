package com.nadoceo.knowledge.presentation;

import com.nadoceo.knowledge.application.CreateFaqUseCase;
import com.nadoceo.knowledge.application.ListFaqsUseCase;
import com.nadoceo.knowledge.application.UpvoteFaqUseCase;
import com.nadoceo.knowledge.presentation.dto.FaqCreateRequest;
import com.nadoceo.knowledge.presentation.dto.FaqResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "FAQ", description = "FAQ 벡터 검색 및 관리 API")
@RestController
@RequestMapping("/api/v1/faq")
public class FaqController {

    private final CreateFaqUseCase createFaq;
    private final ListFaqsUseCase listFaqs;
    private final UpvoteFaqUseCase upvoteFaq;

    public FaqController(CreateFaqUseCase createFaq,
                         ListFaqsUseCase listFaqs,
                         UpvoteFaqUseCase upvoteFaq) {
        this.createFaq = createFaq;
        this.listFaqs = listFaqs;
        this.upvoteFaq = upvoteFaq;
    }

    @Operation(summary = "과목 FAQ 목록 조회")
    @GetMapping("/{courseId}")
    public ResponseEntity<List<FaqResponse>> getFaqsByCourse(@PathVariable UUID courseId) {
        var faqs = listFaqs.execute(courseId).stream().map(FaqResponse::from).toList();
        return ResponseEntity.ok(faqs);
    }

    @Operation(summary = "FAQ 등록", description = "강사가 FAQ를 수동 등록합니다")
    @PostMapping
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqCreateRequest request) {
        var faq = createFaq.execute(new CreateFaqUseCase.Command(
                request.courseId(), request.question(), request.answer(), request.source()));
        var response = FaqResponse.from(faq);
        return ResponseEntity.created(URI.create("/api/v1/faq/" + response.id())).body(response);
    }

    @Operation(summary = "FAQ 좋아요")
    @PostMapping("/{faqId}/upvote")
    public ResponseEntity<Void> upvoteFaq(@PathVariable UUID faqId) {
        upvoteFaq.execute(faqId);
        return ResponseEntity.ok().build();
    }
}
