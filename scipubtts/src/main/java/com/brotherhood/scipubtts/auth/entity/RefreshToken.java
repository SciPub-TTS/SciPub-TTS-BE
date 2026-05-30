package com.brotherhood.scipubtts.auth.entity;

import com.brotherhood.scipubtts.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_token_user_active", columnList = "user_id, revoked_at, expires_at"),
                @Index(name = "idx_refresh_token_family", columnList = "family_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "remember_me", nullable = false)
    private boolean rememberMe;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @PrePersist
    void prePersist() {
        if (issuedAt == null) {
            issuedAt = OffsetDateTime.now();
        }
    }

    public boolean isExpired() {
        return expiredAt != null && expiredAt.isBefore(OffsetDateTime.now());
    }

    public boolean isActive() {
        return revokedAt == null && !isExpired();
    }
}
