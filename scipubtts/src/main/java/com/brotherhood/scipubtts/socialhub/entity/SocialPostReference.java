package com.brotherhood.scipubtts.socialhub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "social_post_reference")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialPostReference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private SocialPost post;

    // Chuẩn hóa về dạng W123456789 — giống module Bookmark
    @Column(name = "openalex_id", nullable = false, length = 50)
    private String openalexId;

    @Column(name = "title_snapshot", columnDefinition = "TEXT")
    private String titleSnapshot;

    @Column(name = "authors_snapshot", columnDefinition = "TEXT")
    private String authorsSnapshot;

    @Column(name = "author_openalex_ids_snapshot", columnDefinition = "TEXT")
    private String authorOpenAlexIdsSnapshot;

    @Column(name = "work_type_snapshot", columnDefinition = "TEXT")
    private String workTypeSnapshot;

    @Column(name = "topic_snapshot", columnDefinition = "TEXT")
    private String topicSnapshot;

    @Column(name = "topic_openalex_id_snapshot", columnDefinition = "TEXT")
    private String topicOpenAlexIdSnapshot;

    @Column(name = "year_snapshot")
    private Short yearSnapshot;

}
