package com.brotherhood.scipubtts.follow.dto.response;

import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FollowResponse(
        UUID id,
        FollowTargetType targetType,
        String targetOpenAlexId,
        String displayName,
        OffsetDateTime createdAt
) {
}
