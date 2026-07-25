package com.brotherhood.scipubtts.admin.repository;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallLogItemResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiCallLogPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public List<AdminApiCallConsumerResponse> findTopApiConsumersFromApiCallLog(
            OffsetDateTime from,
            int limit
    ) {
        return jdbcTemplate.query(
                """
                SELECT u.email, COUNT(*) AS call_count
                FROM api_call_log acl
                JOIN users u ON u.id = acl.user_id
                WHERE acl.caller_type = 'USER'
                  AND acl.user_id IS NOT NULL
                  AND COALESCE(acl.started_at, acl.finished_at) >= ?
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

    public List<AdminApiUsageDailyResponse> findApiUsageDailyFromApiCallLog(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return jdbcTemplate.query(
                """
                SELECT days.usage_date, COALESCE(COUNT(sh.id), 0) AS call_count
                FROM generate_series(?::date, ?::date, interval '1 day') AS days(usage_date)
                LEFT JOIN api_call_log sh
                    ON (COALESCE(sh.started_at, sh.finished_at) AT TIME ZONE 'UTC')::date = days.usage_date
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

    public AdminApiCallLogPageResponse findApiCallLogs(
            int page,
            int size,
            OffsetDateTime from,
            OffsetDateTime to,
            String callerType,
            UUID userId,
            String jobType,
            Integer status,
            String endpoint
    ) {
        StringBuilder where = new StringBuilder(" FROM api_call_log acl LEFT JOIN users u ON u.id = acl.user_id WHERE 1 = 1");
        List<Object> args = new ArrayList<>();

        if (from != null) {
            where.append(" AND COALESCE(acl.started_at, acl.finished_at) >= ?");
            args.add(from);
        }

        if (to != null) {
            where.append(" AND COALESCE(acl.started_at, acl.finished_at) < ?");
            args.add(to);
        }

        if (callerType != null && !callerType.isBlank()) {
            where.append(" AND acl.caller_type = ?");
            args.add(callerType);
        }

        if (userId != null) {
            where.append(" AND acl.user_id = ?");
            args.add(userId);
        }

        if (jobType != null && !jobType.isBlank()) {
            where.append(" AND acl.job_type = ?");
            args.add(jobType);
        }

        if (status != null) {
            where.append(" AND acl.response_status = ?");
            args.add(status);
        }

        if (endpoint != null && !endpoint.isBlank()) {
            where.append(" AND acl.endpoint LIKE ? ESCAPE '\\'");
            args.add(escapeLikePattern(endpoint) + "%");
        }

        long totalElements = count("SELECT COUNT(*)" + where, args.toArray());
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int offset = page * size;

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(size);
        pageArgs.add(offset);

        List<AdminApiCallLogItemResponse> items = jdbcTemplate.query(
                """
                SELECT acl.id,
                       acl.caller_type,
                       acl.user_id,
                       u.email AS user_email,
                       acl.job_id,
                       acl.job_type,
                       acl.method,
                       acl.endpoint,
                       acl.query_params,
                       acl.response_status,
                       acl.records_fetched,
                       acl.duration_ms,
                       acl.started_at,
                       acl.finished_at,
                       acl.error_log
                """ + where + """
                ORDER BY acl.started_at DESC NULLS LAST, acl.id DESC
                LIMIT ? OFFSET ?
                """,
                (rs, rowNum) -> new AdminApiCallLogItemResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("caller_type"),
                        rs.getObject("user_id", UUID.class),
                        rs.getString("user_email"),
                        rs.getObject("job_id", UUID.class),
                        rs.getString("job_type"),
                        rs.getString("method"),
                        rs.getString("endpoint"),
                        rs.getString("query_params"),
                        (Integer) rs.getObject("response_status"),
                        (Integer) rs.getObject("records_fetched"),
                        (Long) rs.getObject("duration_ms"),
                        toOffsetDateTime(rs.getTimestamp("started_at")),
                        toOffsetDateTime(rs.getTimestamp("finished_at")),
                        rs.getString("error_log")
                ),
                pageArgs.toArray()
        );

        return new AdminApiCallLogPageResponse(
                items,
                page,
                size,
                totalElements,
                totalPages,
                page + 1 < totalPages
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

        return Optional.of(toOffsetDateTime(timestamp));
    }

    private OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private String escapeLikePattern(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
