package com.brotherhood.scipubtts.user.service.impl;

import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;
import com.brotherhood.scipubtts.user.dto.response.DashboardResponse;
import com.brotherhood.scipubtts.user.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserBookmarkRepository userBookmarkRepository;
    private final UserFollowRepository userFollowRepository;

    @Override
    public DashboardResponse getSummary(UUID userId) {

        long topics = userFollowRepository.countByUserIdAndTargetType(userId, FollowTargetType.TOPIC);
        long authors = userFollowRepository.countByUserIdAndTargetType(userId, FollowTargetType.AUTHOR);
        long bookmarks = userBookmarkRepository.countByUserId(userId);

        return new  DashboardResponse(topics, authors, bookmarks);
    }

}
