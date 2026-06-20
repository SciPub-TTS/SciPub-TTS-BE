package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.HotTopicItemResponse;
import com.brotherhood.scipubtts.search.dto.HotTopicResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class TopicTrendServiceImpl implements TopicTrendService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;

    private static final String HOT_TOPICS_SQL = """
            WITH latest_weekly_topics AS (
                SELECT
                    topic_id,
                    name,
                field_id,
                works,
                citations,
                velocity,
                acceleration,
                created_at,
                ROW_NUMBER() OVER (
                        PARTITION BY LOWER(name)
                        ORDER BY acceleration DESC, velocity DESC, citations DESC, works DESC, created_at DESC, id DESC
                    ) AS row_rank
                FROM topic
                WHERE DATE(end_time) = ?
            )
            SELECT topic_id, name, field_id, works, citations
            FROM latest_weekly_topics
            WHERE row_rank = 1
            ORDER BY acceleration DESC, velocity DESC, citations DESC, works DESC, name ASC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public TopicTrendServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public HotTopicResponse getWeeklyHotTopics(LocalDate snapshotDate, int limit) {
        LocalDate resolvedSnapshotDate = resolveSnapshotDate(snapshotDate);
        int resolvedLimit = normalizeLimit(limit);

        List<HotTopicItemResponse> hotTopics = jdbcTemplate.query(
                HOT_TOPICS_SQL,
                (resultSet, rowNum) -> new HotTopicItemResponse(
                        resultSet.getString("topic_id"),
                        resultSet.getString("name"),
                        resultSet.getObject("field_id", Integer.class),
                        resultSet.getObject("works", Long.class),
                        resultSet.getObject("citations", Long.class)
                ),
                Date.valueOf(resolvedSnapshotDate),
                resolvedLimit
        );

        return new HotTopicResponse(resolvedSnapshotDate, hotTopics);
    }

    private LocalDate resolveSnapshotDate(LocalDate snapshotDate) {
        if (snapshotDate != null) {
            return snapshotDate;
        }

        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }
}
