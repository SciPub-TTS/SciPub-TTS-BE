package com.brotherhood.scipubtts.search.service.impl;

import com.brotherhood.scipubtts.search.dto.HotKeywordItemResponse;
import com.brotherhood.scipubtts.search.dto.HotKeywordResponse;
import com.brotherhood.scipubtts.search.repository.KeywordTrendReadRepository;
import com.brotherhood.scipubtts.search.service.KeywordTrendService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class KeywordTrendServiceImpl implements KeywordTrendService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;

    private final KeywordTrendReadRepository keywordTrendReadRepository;

    public KeywordTrendServiceImpl(KeywordTrendReadRepository keywordTrendReadRepository) {
        this.keywordTrendReadRepository = keywordTrendReadRepository;
    }

    @Override
    public HotKeywordResponse getWeeklyHotKeywords(LocalDate snapshotDate, int limit) {
        LocalDate resolvedSnapshotDate = resolveSnapshotDate(snapshotDate);
        int resolvedLimit = normalizeLimit(limit);

        List<HotKeywordItemResponse> hotKeywords = keywordTrendReadRepository
                .findTrendingKeywords(
                        resolvedSnapshotDate,
                        PageRequest.of(0, resolvedLimit)
                )
                .stream()
                .map(this::mapHotKeyword)
                .toList();

        return new HotKeywordResponse(resolvedSnapshotDate, hotKeywords);
    }

    private LocalDate resolveSnapshotDate(LocalDate snapshotDate) {
        if (snapshotDate != null) {
            return snapshotDate;
        }

        LocalDate latestSnapshotDate = keywordTrendReadRepository.findLatestSnapshotDate();

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

    private HotKeywordItemResponse mapHotKeyword(
            KeywordTrendReadRepository.KeywordTrendRow keywordTrendRow
    ) {
        return new HotKeywordItemResponse(
                keywordTrendRow.getKeywordId(),
                keywordTrendRow.getKeyword(),
                parseFieldId(keywordTrendRow.getFieldId()),
                keywordTrendRow.getWorksCount(),
                keywordTrendRow.getCitedByCount()
        );
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
