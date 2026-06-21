package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.HotKeywordItemResponse;
import com.brotherhood.scipubtts.search.dto.HotKeywordResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class KeywordTrendServiceImpl implements KeywordTrendService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;
    private static final String LATEST_KEYWORD_SNAPSHOT_SQL = "SELECT MAX(end_time) FROM keywords";

    private static final String TRENDING_KEYWORDS_SQL = """
            WITH latest_weekly_keywords AS (
                SELECT
                    id,
                    keyword_id,
                    keyword,
                    field_id,
                    works_count,
                    cited_by_count,
                    pgr,
                    cagr,
                    ps,
                    start_time,
                    end_time,
                    created_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY LOWER(keyword)
                        ORDER BY cagr DESC, pgr DESC, ps DESC, cited_by_count DESC, works_count DESC, end_time DESC, start_time DESC, created_at DESC, id DESC
                    ) AS row_rank
                FROM keywords
                WHERE end_time = ?
            )
            SELECT keyword_id, keyword, field_id, works_count, cited_by_count
            FROM latest_weekly_keywords
            WHERE row_rank = 1
            ORDER BY cagr DESC, pgr DESC, ps DESC, cited_by_count DESC, works_count DESC, keyword ASC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public KeywordTrendServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public HotKeywordResponse getWeeklyHotKeywords(LocalDate snapshotDate, int limit) {
        LocalDate resolvedSnapshotDate = resolveSnapshotDate(snapshotDate);
        int resolvedLimit = normalizeLimit(limit);

        List<HotKeywordItemResponse> hotKeywords = jdbcTemplate.query(
                TRENDING_KEYWORDS_SQL,
                (resultSet, rowNum) -> new HotKeywordItemResponse(
                        resultSet.getString("keyword_id"),
                        resultSet.getString("keyword"),
                        parseFieldId(resultSet.getString("field_id")),
                        resultSet.getObject("works_count", Long.class),
                        resultSet.getObject("cited_by_count", Long.class)
                ),
                Date.valueOf(resolvedSnapshotDate),
                resolvedLimit
        );

        return new HotKeywordResponse(resolvedSnapshotDate, hotKeywords);
    }

    private LocalDate resolveSnapshotDate(LocalDate snapshotDate) {
        if (snapshotDate != null) {
            return snapshotDate;
        }

        Date latestSnapshot = jdbcTemplate.queryForObject(LATEST_KEYWORD_SNAPSHOT_SQL, Date.class);
        if (latestSnapshot != null) {
            return latestSnapshot.toLocalDate();
        }

        return LocalDate.now();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
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
