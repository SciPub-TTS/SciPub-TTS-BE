package com.brotherhood.scipubtts.bookmark.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "bookmark_collection",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "name"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BookmarkCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
