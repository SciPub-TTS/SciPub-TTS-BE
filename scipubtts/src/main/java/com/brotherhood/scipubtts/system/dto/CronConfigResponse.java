package com.brotherhood.scipubtts.system.dto;

import lombok.Builder;

@Builder
public record CronConfigResponse(
        String configKey,
        String fullCronExpression,
        String second,
        String minute,
        String hour,
        String dayOfMonth,
        String month,
        String dayOfWeek,
        String description
) {}