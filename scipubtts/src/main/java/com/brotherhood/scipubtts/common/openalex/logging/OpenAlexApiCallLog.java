package com.brotherhood.scipubtts.common.openalex.logging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_call_log")
@Getter
@Setter
public class OpenAlexApiCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "caller_type", nullable = false, length = 20)
    private String callerType;

    @Column(name = "job_type", length = 80)
    private String jobType;

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "endpoint", nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "query_params", columnDefinition = "TEXT")
    private String queryParams;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "records_fetched")
    private Integer recordsFetched;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;
}
