package com.brotherhood.scipubtts.admin.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminApiCallLogItemResponse(
        UUID id,
        String callerType,
        UUID userId,
        String userEmail,
        UUID jobId,
        String jobType,
        String method,
        String endpoint,
        String queryParams,
        Integer responseStatus,
        Integer recordsFetched,
        Long durationMs,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        String errorLog
) {
}
