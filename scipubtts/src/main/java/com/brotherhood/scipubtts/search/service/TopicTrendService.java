package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.response.HotTopicResponse;

import java.time.LocalDate;

public interface TopicTrendService {
    HotTopicResponse getWeeklyHotTopics(LocalDate snapshotDate, int limit);
}
