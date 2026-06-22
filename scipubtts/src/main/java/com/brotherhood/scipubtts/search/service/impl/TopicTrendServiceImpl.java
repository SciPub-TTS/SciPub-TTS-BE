package com.brotherhood.scipubtts.search.service.impl;

import com.brotherhood.scipubtts.search.dto.HotTopicItemResponse;
import com.brotherhood.scipubtts.search.dto.HotTopicResponse;
import com.brotherhood.scipubtts.search.repository.TopicTrendReadRepository;
import com.brotherhood.scipubtts.search.service.TopicTrendService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TopicTrendServiceImpl implements TopicTrendService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;

    private final TopicTrendReadRepository topicTrendReadRepository;

    public TopicTrendServiceImpl(TopicTrendReadRepository topicTrendReadRepository) {
        this.topicTrendReadRepository = topicTrendReadRepository;
    }

    @Override
    public HotTopicResponse getWeeklyHotTopics(LocalDate snapshotDate, int limit) {
        LocalDate resolvedSnapshotDate = resolveSnapshotDate(snapshotDate);
        int resolvedLimit = normalizeLimit(limit);

        List<HotTopicItemResponse> hotTopics = topicTrendReadRepository
                .findTrendingTopics(
                        resolvedSnapshotDate,
                        PageRequest.of(0, resolvedLimit)
                )
                .stream()
                .map(this::mapHotTopic)
                .toList();

        return new HotTopicResponse(resolvedSnapshotDate, hotTopics);
    }

    private LocalDate resolveSnapshotDate(LocalDate snapshotDate) {
        if (snapshotDate != null) {
            return snapshotDate;
        }

        LocalDate latestSnapshotDate = topicTrendReadRepository.findLatestSnapshotDate();

        if (latestSnapshotDate != null) {
            return latestSnapshotDate;
        }

        return LocalDate.now();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private HotTopicItemResponse mapHotTopic(
            TopicTrendReadRepository.TopicTrendRow topicTrendRow
    ) {
        return new HotTopicItemResponse(
                topicTrendRow.getTopicId(),
                topicTrendRow.getName(),
                topicTrendRow.getFieldId(),
                topicTrendRow.getWorks(),
                topicTrendRow.getCitations()
        );
    }
}
