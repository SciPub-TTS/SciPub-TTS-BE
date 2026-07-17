package com.brotherhood.scipubtts.system.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCronConfigRequest(
        @NotBlank String second,
        @NotBlank String minute,
        @NotBlank String hour,
        String dayOfMonth,
        String month,
        String dayOfWeek
) {
}
