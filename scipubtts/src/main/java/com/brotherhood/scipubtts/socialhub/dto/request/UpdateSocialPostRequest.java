package com.brotherhood.scipubtts.socialhub.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSocialPostRequest(
        @Size(min = 10, max = 100, message = "Title must be between 10 and 100 characters")
        String title,

        @Size(min = 20, message = "Body must be at least 20 characters")
        String body,

        @Size(max = 1000, message = "Topic tag cannot exceed 1000 characters")
        String topicTag
) {
}
