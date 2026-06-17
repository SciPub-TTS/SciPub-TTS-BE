package com.brotherhood.scipubtts.dashboard.dto.response;

import com.brotherhood.scipubtts.dashboard.entity.Keyword;

import java.util.List;

public record KeywordCalculateResponse(
        List<Keyword> keywordList
) {
}
