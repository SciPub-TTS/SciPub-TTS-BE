package com.brotherhood.scipubtts.journalDaily.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu bài viết khoa học/công nghệ từ The Guardian API.
 * external_id là "id" của Guardian (dạng "technology/2026/jul/29/...")
 * được dùng làm unique key để CronJob check trùng trước khi lưu.
 */
@Entity
@Table(
        name = "guardian_article",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_guardian_article_ext_id",
                columnNames = "external_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalDailyArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guardian's own id — dùng để check trùng, tránh lưu lại bài đã có
    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    // fields.trailText — đoạn tóm tắt
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // fields.byline — tên tác giả
    @Column(name = "author", length = 255)
    private String author;

    // fields.thumbnail — URL ảnh đại diện
    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    // webUrl — link gốc bài viết trên The Guardian
    @Column(name = "source_url", nullable = false, length = 1000)
    private String sourceUrl;

    // webPublicationDate — lưu dạng UTC, hiển thị FE tự convert sang dd/MM/yyyy Việt Nam
    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    // sectionName — "Technology", "Science"
    @Column(name = "category", length = 100)
    private String category;

    // Thời điểm CronJob cào về DB
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // tags[].webTitle — 1 bài có nhiều tag
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "article",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<JournalDailyArticleTag> tags = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // Helper: thêm tag và set FK về article này
    public void addTag(String tagName) {
        JournalDailyArticleTag tag = JournalDailyArticleTag.builder()
                .article(this)
                .tagName(tagName)
                .build();
        this.tags.add(tag);
    }


}