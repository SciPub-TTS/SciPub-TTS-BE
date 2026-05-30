package com.brotherhood.scipubtts.auth.repository;

import com.brotherhood.scipubtts.auth.entity.PasswordResetChallenge;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetChallengeRepository extends JpaRepository<PasswordResetChallenge, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetChallenge> findTopByUserIdAndInvalidatedAtIsNullOrderByCreatedAtDesc(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PasswordResetChallenge c
           set c.invalidatedAt = :now
         where c.user.id = :userId
           and c.invalidatedAt is null
           and c.expiresAt > :now
    """)
    int invalidateActiveByUserId(@Param("userId") UUID userId,
                                 @Param("now") OffsetDateTime now);
}
