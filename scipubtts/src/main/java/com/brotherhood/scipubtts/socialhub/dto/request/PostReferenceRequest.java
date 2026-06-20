package com.brotherhood.scipubtts.socialhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PostReferenceRequest(
        @NotBlank(message = "OpenAlex ID is required")
        @Pattern(
                regexp = "^(https://openalex\\.org/)?[Ww]\\d+$",
                message = "Invalid OpenAlex Work ID format (e.g. W123456789)"
        )
        String openalexId
) {
}
