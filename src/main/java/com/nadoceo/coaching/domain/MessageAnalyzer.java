package com.nadoceo.coaching.domain;

public final class MessageAnalyzer {

    private MessageAnalyzer() {}

    public enum MessageType {
        UNCLEAR, HAS_ERROR, FOLLOW_UP, TERM_QUERY
    }

    public static MessageType analyze(String message) {
        String lower = message.toLowerCase();

        if (containsErrorPattern(lower)) return MessageType.HAS_ERROR;
        if (isTermQuery(lower)) return MessageType.TERM_QUERY;
        if (message.length() < 20 || isVagueQuestion(lower)) return MessageType.UNCLEAR;
        return MessageType.FOLLOW_UP;
    }

    private static boolean containsErrorPattern(String message) {
        return message.contains("error") || message.contains("에러")
                || message.contains("exception") || message.contains("오류")
                || message.contains("null") || message.contains("npe")
                || message.contains("stacktrace") || message.contains("stack trace")
                || message.contains("failed") || message.contains("실패")
                || message.matches(".*\\bat\\s+\\w+\\.\\w+.*");
    }

    private static boolean isTermQuery(String message) {
        return message.contains("뭐야") || message.contains("뭔가요")
                || message.contains("이란") || message.contains("이 뭐")
                || message.contains("what is") || message.contains("개념")
                || message.contains("정의") || message.contains("차이");
    }

    private static boolean isVagueQuestion(String message) {
        return message.contains("안돼") || message.contains("안 돼")
                || message.contains("모르겠") || message.contains("왜 그래")
                || message.contains("도와줘") || message.contains("help");
    }
}
