package com.brotherhood.scipubtts.bookmark.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_bookmark", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "openalex_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "openalex_id", nullable = false, columnDefinition = "TEXT")
    private String openAlexId;

    @Column(name = "entity_type", nullable = false)
    @Builder.Default
    private String entityType = "WORK";

    @Column(name = "title_snapshot", columnDefinition = "TEXT")
    private String titleSnapshot;

    @Column(name = "authors_snapshot", columnDefinition = "TEXT")
    private String authorsSnapshot;

    @Column(name = "topic_snapshot", columnDefinition = "TEXT")
    private String topicSnapshot;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "citation_snapshot")
    private Integer citationSnapshot;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

}
