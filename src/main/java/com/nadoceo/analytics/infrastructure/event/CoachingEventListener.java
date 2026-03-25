package com.nadoceo.analytics.infrastructure.event;

import com.nadoceo.analytics.application.RecordEventUseCase;
import com.nadoceo.analytics.domain.EventType;
import com.nadoceo.coaching.domain.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * coaching 컨텍스트의 도메인 이벤트를 구독하여
 * analytics 컨텍스트의 학습 경로 이벤트로 기록한다.
 */
@Component
public class CoachingEventListener {

    private final RecordEventUseCase recordEvent;

    public CoachingEventListener(RecordEventUseCase recordEvent) {
        this.recordEvent = recordEvent;
    }

    @Async
    @EventListener
    public void on(StudentQuestionAsked event) {
        recordEvent.execute(event.sessionId(), event.studentId(), event.courseId(),
                EventType.STUDENT_QUESTION, event.turnNumber(), event.message(), null, null);
    }

    @Async
    @EventListener
    public void on(SocraticQuestionDelivered event) {
        recordEvent.execute(event.sessionId(), event.studentId(), event.courseId(),
                EventType.SOCRATIC_QUESTION, event.turnNumber(), "[AI 소크라테스 질문]", null, null);
    }

    @Async
    @EventListener
    public void on(FaqSearchRequested event) {
        if (event.hit()) {
            recordEvent.execute(event.sessionId(), event.studentId(), event.courseId(),
                    EventType.FAQ_HIT, event.turnNumber(), event.query(),
                    event.faqId(), "{\"similarity\": " + event.similarity() + "}");
        } else {
            recordEvent.execute(event.sessionId(), event.studentId(), event.courseId(),
                    EventType.FAQ_MISS, event.turnNumber(), event.query(), null, null);
        }
    }

    @Async
    @EventListener
    public void on(TermLookupRequested event) {
        recordEvent.execute(event.sessionId(), event.studentId(), event.courseId(),
                EventType.TERM_SEARCHED, event.turnNumber(), event.term(), null, null);
    }

    @Async
    @EventListener
    public void on(SessionResolved event) {
        recordEvent.execute(event.sessionId(), event.studentId(), event.courseId(),
                EventType.RESOLVED, event.totalTurns(), event.summary(), null, null);
    }
}
