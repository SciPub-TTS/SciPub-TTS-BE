package com.brotherhood.scipubtts.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "keywords",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_keyword_snapshot",
                columnNames = {"keyword_id", "field_id", "start_time", "end_time"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "keyword_id", nullable = false, length = 500)
  private String keywordId;

  @Column(nullable = false, length = 500)
  private String keyword;

  @Column(name = "field_id", nullable = false, length = 100)
  private String fieldId;

  @Column(name = "start_time", nullable = false)
  private LocalDate startTime;

  @Column(name = "end_time", nullable = false)
  private LocalDate endTime;

  // Metric Group 1: Publication Growth Rate
  @Column(name = "pgr")
  private Double pgr;

  @Column(name = "cagr")
  private Double cagr;

  // Metric Group 2: Publication Share
  @Column(name = "ps")
  private Double ps;

  // Raw counts
  @Column(name = "works_count", nullable = false)
  private Long worksCount;

  @Column(name = "cited_by_count", nullable = false)
  private Long citedByCount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}