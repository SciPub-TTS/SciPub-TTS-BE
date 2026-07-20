package com.brotherhood.scipubtts.detail.authors.service;

import com.brotherhood.scipubtts.detail.authors.dto.response.AuthorDetailResponse;

public interface AuthorDetailService {
    AuthorDetailResponse getAuthorDetail(String rawAuthorId);
}
