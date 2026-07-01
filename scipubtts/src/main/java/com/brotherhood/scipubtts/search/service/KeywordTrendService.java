package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.response.HotKeywordResponse;

import java.time.LocalDate;

public interface KeywordTrendService {
    HotKeywordResponse getWeeklyHotKeywords(LocalDate snapshotDate, int limit);
}
