package com.brotherhood.scipubtts.detail.entities.service;

import com.brotherhood.scipubtts.detail.entities.dto.response.EntityDetailResponse;

public interface EntityDetailService {
    EntityDetailResponse getAuthorDetail(String rawAuthorId);

    EntityDetailResponse getTopicDetail(String rawTopicId);
}
