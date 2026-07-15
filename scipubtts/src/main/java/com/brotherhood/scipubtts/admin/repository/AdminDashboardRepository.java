package com.brotherhood.scipubtts.admin.repository;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminDashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countUsers() {
        return count("SELECT COUNT(*) FROM users");
    }

    public long countBannedUsers() {
        return count("SELECT COUNT(*) FROM users WHERE is_banned = true");
    }

    public long countActiveUsers() {
        return count("SELECT COUNT(*) FROM users WHERE is_banned = false");
    }

    public long countApiCallsFrom(OffsetDateTime from) {
        return count(
                """
                SELECT COUNT(*)
                FROM api_call_log
                WHERE COALESCE(started_at, finished_at) >= ?
                """,
                from
        );
    }

    public List<AdminApiCallConsumerResponse> findTopApiConsumersFromSearchHistory(
            OffsetDateTime from,
            int limit
    ) {
        return jdbcTemplate.query(
                """
                SELECT u.email, COUNT(*) AS call_count
                FROM search_history sh
                JOIN users u ON u.id = sh.user_id
                WHERE sh.user_id IS NOT NULL
                  AND sh.created_at >= ?
                GROUP BY u.id, u.email
                ORDER BY call_count DESC, u.email ASC
                LIMIT ?
                """,
                (rs, rowNum) -> new AdminApiCallConsumerResponse(
                        rs.getString("email"),
                        rs.getLong("call_count")
                ),
                from,
                limit
        );
    }

    public List<AdminApiUsageDailyResponse> findApiUsageDailyFromSearchHistory(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return jdbcTemplate.query(
                """
                SELECT days.usage_date, COALESCE(COUNT(sh.id), 0) AS call_count
                FROM generate_series(?::date, ?::date, interval '1 day') AS days(usage_date)
                LEFT JOIN search_history sh
                    ON sh.created_at >= days.usage_date
                   AND sh.created_at < days.usage_date + interval '1 day'
                GROUP BY days.usage_date
                ORDER BY days.usage_date ASC
                """,
                (rs, rowNum) -> new AdminApiUsageDailyResponse(
                        rs.getDate("usage_date").toLocalDate(),
                        rs.getLong("call_count")
                ),
                startDate,
                endDate
        );
    }

    public Optional<OffsetDateTime> findLatestSynchronization() {
        Optional<OffsetDateTime> latestJob = queryOptionalOffsetDateTime(
                """
                SELECT MAX(finished_at)
                FROM api_job
                WHERE status = 'SUCCESS'
                """
        );

        if (latestJob.isPresent()) {
            return latestJob;
        }

        return queryOptionalOffsetDateTime(
                "SELECT MAX(finished_at) FROM api_call_log"
        );
    }

    private long count(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private Optional<OffsetDateTime> queryOptionalOffsetDateTime(String sql) {
        Timestamp timestamp = jdbcTemplate.queryForObject(sql, Timestamp.class);
        if (timestamp == null) {
            return Optional.empty();
        }

        return Optional.of(timestamp.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());
    }
}
