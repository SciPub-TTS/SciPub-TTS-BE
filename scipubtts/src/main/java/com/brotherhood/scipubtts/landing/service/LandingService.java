package com.brotherhood.scipubtts.landing.service;

import com.brotherhood.scipubtts.landing.dto.response.LandingKeywordPreviewItemResponse;
import com.brotherhood.scipubtts.landing.dto.response.LandingTopicPreviewItemResponse;
import com.brotherhood.scipubtts.landing.dto.response.LandingTrendPreviewResponse;
import com.brotherhood.scipubtts.search.repository.KeywordTrendReadRepository;
import com.brotherhood.scipubtts.search.repository.TopicTrendReadRepository;
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

    public LandingTrendPreviewResponse getTrendPreview(LocalDate snapshotDate) {
        LocalDate resolvedSnapshotDate = resolveSnapshotDate(snapshotDate);

        List<LandingTopicPreviewItemResponse> topTopics = topicTrendReadRepository
                .findTrendingTopics(
                        resolvedSnapshotDate,
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

        List<LandingKeywordPreviewItemResponse> topKeywords = keywordTrendReadRepository
                .findTrendingKeywords(
                        resolvedSnapshotDate,
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
                resolvedSnapshotDate,
                topicTrendReadRepository.countTrendingTopics(resolvedSnapshotDate),
                keywordTrendReadRepository.countTrendingKeywords(resolvedSnapshotDate),
                topTopics,
                topKeywords
        );
    }

    private LocalDate resolveSnapshotDate(LocalDate snapshotDate) {
        if (snapshotDate != null) {
            return snapshotDate;
        }

        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
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
