package com.brotherhood.scipubtts.follow.service;

import com.brotherhood.scipubtts.follow.dto.request.CreateFollowRequest;
import com.brotherhood.scipubtts.follow.dto.response.FollowPageResponse;
import com.brotherhood.scipubtts.follow.dto.response.FollowResponse;
import com.brotherhood.scipubtts.follow.dto.response.FollowStatusResponse;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;

import java.util.UUID;

public interface FollowService {

    FollowResponse followTarget(UUID userId, CreateFollowRequest request);

    void unfollowTarget(UUID userId, FollowTargetType targetType, String targetOpenAlexId);

    FollowStatusResponse getFollowStatus(UUID userId, FollowTargetType targetType, String targetOpenAlexId);

    FollowPageResponse getMyFollows(
            UUID userId,
            int page,
            int size,
            String keyword,
            FollowTargetType targetType,
            String sort
    );
}
