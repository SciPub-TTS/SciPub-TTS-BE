package com.brotherhood.scipubtts.auth.repository;

import com.brotherhood.scipubtts.auth.entity.PasswordResetGrant;
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
public interface PasswordResetGrantRepository extends JpaRepository<PasswordResetGrant, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetGrant> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PasswordResetGrant g
           set g.revokedAt = :now
         where g.user.id = :userId
           and g.revokedAt is null
           and g.consumedAt is null
           and g.expiredAt > :now
    """)
    int revokeActiveByUserId(@Param("userId") UUID userId,
                             @Param("now") OffsetDateTime now);
}
