package com.brotherhood.scipubtts.follow.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.follow.dto.request.CreateFollowRequest;
import com.brotherhood.scipubtts.follow.dto.response.FollowPageResponse;
import com.brotherhood.scipubtts.follow.dto.response.FollowResponse;
import com.brotherhood.scipubtts.follow.dto.response.FollowStatusResponse;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.entity.UserFollow;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;
import com.brotherhood.scipubtts.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 50;

    private final UserFollowRepository userFollowRepository;

    public FollowResponse toResponse(UserFollow follow) {
        if (follow == null) {
            return null;
        }

        return new FollowResponse(
                follow.getId(),
                follow.getTargetType(),
                follow.getTargetOpenAlexId(),
                follow.getDisplayNameSnapshot(),
                follow.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public FollowResponse followTarget(UUID userId, CreateFollowRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.REQUEST_BODY_REQUIRED);
        }

        String openAlexId = normalize(request.targetOpenalexId());
        FollowTargetType targetType = request.targetType();

        return userFollowRepository.findByUserIdAndTargetTypeAndTargetOpenAlexId(userId, targetType, openAlexId)
                .map(this::toResponse)
                .orElseGet(() -> createNewFollow(userId, targetType, openAlexId, request));
    }

    private FollowResponse createNewFollow(
            UUID userId,
            FollowTargetType targetType,
            String openAlexId,
            CreateFollowRequest request
    ) {
        try {
            UserFollow follow = UserFollow.builder()
                    .userId(userId)
                    .targetType(targetType)
                    .targetOpenAlexId(openAlexId)
                    .displayNameSnapshot(normalizeNullable(request.displayName()))
                    .createdAt(OffsetDateTime.now())
                    .build();

            UserFollow saved = userFollowRepository.save(follow);
            return this.toResponse(saved);

        } catch (DataIntegrityViolationException ex) {
            UserFollow existing = userFollowRepository
                    .findByUserIdAndTargetTypeAndTargetOpenAlexId(userId, targetType, openAlexId)
                    .orElseThrow(() -> ex);

            return this.toResponse(existing);
        }
    }

    @Override
    @Transactional
    public void unfollowTarget(UUID userId, FollowTargetType targetType, String targetOpenAlexId) {
        String normalizedOpenAlexId = normalize(targetOpenAlexId);
        userFollowRepository.deleteByUserIdAndTargetTypeAndTargetOpenAlexId(userId, targetType, normalizedOpenAlexId);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowStatusResponse getFollowStatus(UUID userId, FollowTargetType targetType, String targetOpenAlexId) {
        String normalizedOpenAlexId = normalize(targetOpenAlexId);

        if (!StringUtils.hasText(normalizedOpenAlexId) || targetType == null) {
            return new FollowStatusResponse(false, null, targetType, targetOpenAlexId);
        }

        return userFollowRepository.findByUserIdAndTargetTypeAndTargetOpenAlexId(userId, targetType, normalizedOpenAlexId)
                .map(follow -> new FollowStatusResponse(
                        true,
                        follow.getId(),
                        follow.getTargetType(),
                        follow.getTargetOpenAlexId()
                ))
                .orElseGet(() -> new FollowStatusResponse(
                        false,
                        null,
                        targetType,
                        normalizedOpenAlexId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public FollowPageResponse getMyFollows(
            UUID userId,
            int page,
            int size,
            String keyword,
            FollowTargetType targetType,
            String sort
    ) {
        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = normalizeSize(size);

        Sort springSort = buildSort(sort);
        Pageable pageable = PageRequest.of(safePage, safeSize, springSort);

        Page<UserFollow> followPage = userFollowRepository.searchMyFollows(
                userId,
                normalizeNullable(keyword),
                targetType,
                pageable
        );

        List<FollowResponse> items = followPage
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new FollowPageResponse(
                items,
                followPage.getNumber(),
                followPage.getSize(),
                followPage.getTotalElements(),
                followPage.getTotalPages(),
                followPage.hasNext()
        );
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private Sort buildSort(String sort) {
        String normalizedSort = normalize(sort);

        if (normalizedSort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return Objects.equals(normalizedSort.toUpperCase(), "OLDEST")
                ? Sort.by(Sort.Direction.ASC, "createdAt")
                : Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
