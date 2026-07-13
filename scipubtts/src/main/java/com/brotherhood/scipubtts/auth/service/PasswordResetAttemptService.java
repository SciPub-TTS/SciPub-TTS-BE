package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.service.impl.PasswordResetAttemptServiceImpl;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PasswordResetAttemptService {
    PasswordResetAttemptServiceImpl.FailedAttemptResult persistFailedAttempt(
            UUID challengeId,
            OffsetDateTime now
    );

    void invalidateChallenge(
            UUID challengeId,
            OffsetDateTime now
    );

}
