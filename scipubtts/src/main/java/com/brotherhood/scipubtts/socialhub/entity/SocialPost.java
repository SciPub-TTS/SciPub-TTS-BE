package com.brotherhood.scipubtts.socialhub.entity;

import com.brotherhood.scipubtts.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "social_post")
@SQLRestriction("deleted_at IS NULL")   // Hibernate 6 — tự động filter soft-delete
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "topic_tag", length = 500)
    private String topicTag;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SocialPostReference> references = new ArrayList<>();

    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Soft-delete helper
    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    public void resetLikeCount() {
        this.likeCount = 0;
    }
}
