package com.brotherhood.scipubtts.auth.repository;

import com.brotherhood.scipubtts.auth.entity.GoogleSignupToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GoogleSignupTokenRepository extends JpaRepository<GoogleSignupToken, UUID> {

    Optional<GoogleSignupToken> findByTokenHash(String tokenHash);

}
