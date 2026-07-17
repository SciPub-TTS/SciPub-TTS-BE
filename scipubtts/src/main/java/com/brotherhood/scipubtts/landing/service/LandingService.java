package com.brotherhood.scipubtts.landing.service;

import com.brotherhood.scipubtts.landing.dto.response.LandingKeywordPreviewItemResponse;
import com.brotherhood.scipubtts.landing.dto.response.LandingTopicPreviewItemResponse;
import com.brotherhood.scipubtts.landing.dto.response.LandingTrendPreviewResponse;
import com.brotherhood.scipubtts.dashboard.repository.KeywordTrendReadRepository;
import com.brotherhood.scipubtts.dashboard.repository.TopicTrendReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LandingService {

    private static final int DEFAULT_TOPIC_PREVIEW_LIMIT = 5;
    private static final int DEFAULT_KEYWORD_PREVIEW_LIMIT = 5;

    private final TopicTrendReadRepository topicTrendReadRepository;
    private final KeywordTrendReadRepository keywordTrendReadRepository;

    public LandingTrendPreviewResponse getTrendPreview() {
        LocalDate topicSnapshotDate = topicTrendReadRepository.findLatestSnapshotDate();
        LocalDate keywordSnapshotDate = keywordTrendReadRepository.findLatestSnapshotDate();
        LocalDate responseSnapshotDate = resolveResponseSnapshotDate(
                topicSnapshotDate,
                keywordSnapshotDate
        );

        List<LandingTopicPreviewItemResponse> topTopics = topicSnapshotDate == null
                ? List.of()
                : topicTrendReadRepository
                        .findTrendingTopics(
                                topicSnapshotDate,
                                PageRequest.of(0, DEFAULT_TOPIC_PREVIEW_LIMIT)
                        )
                        .stream()
                        .map(topic -> new LandingTopicPreviewItemResponse(
                                topic.getTopicId(),
                                topic.getName(),
                                topic.getFieldId(),
                                topic.getWorks(),
                                topic.getCitations()
                        ))
                        .toList();

        List<LandingKeywordPreviewItemResponse> topKeywords = keywordSnapshotDate == null
                ? List.of()
                : keywordTrendReadRepository
                        .findTrendingKeywords(
                                keywordSnapshotDate,
                                PageRequest.of(0, DEFAULT_KEYWORD_PREVIEW_LIMIT)
                        )
                        .stream()
                        .map(keyword -> new LandingKeywordPreviewItemResponse(
                                keyword.getKeywordId(),
                                keyword.getKeyword(),
                                parseFieldId(keyword.getFieldId()),
                                keyword.getWorksCount(),
                                keyword.getCitedByCount()
                        ))
                        .toList();

        return new LandingTrendPreviewResponse(
                responseSnapshotDate,
                topicSnapshotDate == null
                        ? 0L
                        : topicTrendReadRepository.countTrendingTopics(topicSnapshotDate),
                keywordSnapshotDate == null
                        ? 0L
                        : keywordTrendReadRepository.countTrendingKeywords(keywordSnapshotDate),
                topTopics,
                topKeywords
        );
    }

    private LocalDate resolveResponseSnapshotDate(
            LocalDate topicSnapshotDate,
            LocalDate keywordSnapshotDate
    ) {
        if (topicSnapshotDate == null) {
            return keywordSnapshotDate != null
                    ? keywordSnapshotDate
                    : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        if (keywordSnapshotDate == null) {
            return topicSnapshotDate;
        }

        return topicSnapshotDate.isAfter(keywordSnapshotDate)
                ? topicSnapshotDate
                : keywordSnapshotDate;
    }

    private Integer parseFieldId(String fieldId) {
        if (fieldId == null || fieldId.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(fieldId.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
