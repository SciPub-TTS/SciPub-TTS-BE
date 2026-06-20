package com.brotherhood.scipubtts.follow.dto.request;

import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFollowRequest(
        @NotNull(message = "Target type is required")
        FollowTargetType targetType,

        @NotBlank(message = "Target OpenAlex ID is required")
        String targetOpenalexId,

        String displayName
) {
}
