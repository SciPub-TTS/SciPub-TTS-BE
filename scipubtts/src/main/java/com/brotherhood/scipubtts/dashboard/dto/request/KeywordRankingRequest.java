package com.brotherhood.scipubtts.dashboard.dto.request;

import java.time.LocalDate;

public record KeywordRankingRequest(
        LocalDate startTime,
        LocalDate endTime,
        String fieldId,
        String formula
) {
}