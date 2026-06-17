package com.brotherhood.scipubtts.feed.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "request_params", columnDefinition = "jsonb")
    private String requestParams;

    @Column(name = "total_fetched")
    private Integer totalFetched;

    @Column(name = "total_saved")
    private Integer totalSaved;

    @Column(name = "total_failed")
    private Integer totalFailed;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "error_log")
    private String errorLog;
}