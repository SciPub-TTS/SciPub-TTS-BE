package com.brotherhood.scipubtts.socialhub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateSocialPostRequest(
        String title,

        String body,

        @Size(max = 500, message = "Topic tag cannot exceed 500 characters")
        String topicTag,

        @Size(max = 3, message = "Maximum 3 references per post")
        @Valid
        List<String> references
) {
}
