package com.brotherhood.scipubtts.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.RESEARCHER;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "password_hash", length = 255)
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
