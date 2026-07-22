package com.brotherhood.scipubtts.detail.topics.service;

import com.brotherhood.scipubtts.detail.topics.dto.response.TopicDetailResponse;

public interface TopicDetailService {
    TopicDetailResponse getTopicDetail(String rawTopicId);
}
