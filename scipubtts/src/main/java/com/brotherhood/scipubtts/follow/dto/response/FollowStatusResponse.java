package com.brotherhood.scipubtts.follow.dto.response;

import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import java.util.UUID;

public record FollowStatusResponse(
        boolean followed,
        UUID followId,
        FollowTargetType targetType,
        String targetOpenAlexId
) {
}
