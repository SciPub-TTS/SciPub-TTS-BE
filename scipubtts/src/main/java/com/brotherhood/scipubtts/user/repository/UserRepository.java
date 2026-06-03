package com.brotherhood.scipubtts.user.repository;

import com.brotherhood.scipubtts.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String username);

    boolean existsByEmail(String email);

    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(OffsetDateTime threshold);
}
