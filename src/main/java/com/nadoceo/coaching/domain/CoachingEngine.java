package com.nadoceo.coaching.domain;

import com.nadoceo.coaching.domain.MessageAnalyzer.MessageType;

/**
 * 소크라테스식 코칭 판단 엔진 — 순수 Domain Service.
 * Spring 의존성 없음.
 */
public final class CoachingEngine {

    public CoachingState determine(String message, ChatType chatType, int currentTurn) {
        if (chatType == ChatType.SUB) {
            return CoachingState.TERM_LOOKUP;
        }

        MessageType type = MessageAnalyzer.analyze(message);

        return switch (type) {
            case UNCLEAR    -> CoachingState.SOCRATIC;
            case HAS_ERROR  -> CoachingState.FAQ_SEARCH;
            case FOLLOW_UP  -> CoachingState.CONTINUE;
            case TERM_QUERY -> CoachingState.TERM_LOOKUP;
        };
    }
}
