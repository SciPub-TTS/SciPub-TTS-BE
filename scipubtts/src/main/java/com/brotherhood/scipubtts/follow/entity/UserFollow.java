package com.brotherhood.scipubtts.follow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_follow", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "target_type", "target_openalex_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private FollowTargetType targetType;

    @Column(name = "target_openalex_id", nullable = false, columnDefinition = "TEXT")
    private String targetOpenAlexId;

    @Column(name = "display_name_snapshot", columnDefinition = "TEXT")
    private String displayNameSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

}
