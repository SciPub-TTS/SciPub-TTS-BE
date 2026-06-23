package com.brotherhood.scipubtts.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.RESEARCHER;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified = false;

    // Map field rõ nghĩa vào column DB hiện tại
    @Column(name = "is_google_authenticated", nullable = false)
    private boolean googleLinked = false;

    @Column(name = "is_banned", nullable = false)
    private boolean banned = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

}
