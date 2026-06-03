    package com.brotherhood.scipubtts.auth.entity;

    import com.brotherhood.scipubtts.user.entity.User;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.OffsetDateTime;
    import java.util.UUID;

    @Entity
    @Table(
            name = "password_reset_grant",
            indexes = {
                    @Index(name = "idx_prg_user_created", columnList = "user_id, created_at")
            }
    )
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PasswordResetGrant {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @OneToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "challenge_id", nullable = false)
        private PasswordResetChallenge challengeId;

        @Column(name = "token_hash", nullable = false, unique = true, length = 64)
        private String tokenHash;

        @Column(name = "expired_at", nullable = false)
        private OffsetDateTime expiredAt;

        @Column(name = "consumed_at")
        private OffsetDateTime consumedAt;

        @Column(name = "revoked_at")
        private OffsetDateTime revokedAt;

        @Column(name = "created_at", nullable = false)
        private OffsetDateTime createdAt;

        @PrePersist
        void prePersist() {
            if (createdAt == null) {
                createdAt = OffsetDateTime.now();
            }
        }

        public boolean isExpired() {
            return expiredAt != null && expiredAt.isBefore(OffsetDateTime.now());
        }

        public boolean isActive() {
            return consumedAt == null && revokedAt == null && !isExpired();
        }
    }
