package com.brotherhood.scipubtts.feed.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "research_feed_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchFeed {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "work_openalex_id", nullable = false, columnDefinition = "TEXT")
  private String workOpenAlexId;

  @Column(name = "title_snapshot", columnDefinition = "TEXT")
  private String titleSnapshot;

  @Column(name = "authors_snapshot", columnDefinition = "TEXT")
  private String authorsSnapshot;

  @Column(name = "source_snapshot", columnDefinition = "TEXT")
  private String sourceSnapshot;

  @Column(name = "publication_year")
  private Integer publicationYear;

  @Column(name = "citation_snapshot")
  private Integer citationSnapshot;

  @Column(name = "generated_at", nullable = false, updatable = false)
  private OffsetDateTime generatedAt;

  @Column(name = "publication_date")
  private LocalDate publicationDate;

  @Column(name = "reason_json", columnDefinition = "jsonb")
  private String reasonJson;

  @Column(name = "relevance_score")
  private Double relevanceScore;

  @Column(name = "is_seen", nullable = false)
  private Boolean isSeen;

  @Column(name = "dismissed_at")
  private OffsetDateTime dismissedAt;
}
