package com.brotherhood.scipubtts.detail.works.service;

import com.brotherhood.scipubtts.detail.works.dto.response.PaperDetailResponse;

public interface PaperDetailService {
    PaperDetailResponse getWorkDetail(String workId);
}
