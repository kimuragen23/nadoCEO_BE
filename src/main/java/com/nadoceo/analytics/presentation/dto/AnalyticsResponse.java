package com.nadoceo.analytics.presentation.dto;

public class AnalyticsResponse {
    public record TermCount(String term, long count) {}
    public record FaqHitRate(String month, double hitRatePercent) {}
    public record EventCount(String eventType, long count) {}
    public record AverageTurns(double averageTurns) {}
}
