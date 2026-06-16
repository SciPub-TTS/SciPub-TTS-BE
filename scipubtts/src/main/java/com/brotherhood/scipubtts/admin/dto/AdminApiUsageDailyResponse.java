package com.brotherhood.scipubtts.admin.dto;

import java.time.LocalDate;

public record AdminApiUsageDailyResponse(
        LocalDate date,
        long callCount
) {
}
