package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.entity.PasswordResetChallenge;
import com.brotherhood.scipubtts.auth.service.impl.PasswordResetAttemptServiceImpl;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PasswordResetAttemptService {
    public PasswordResetAttemptServiceImpl.FailedAttemptResult persistFailedAttempt(
            UUID challengeId,
            OffsetDateTime now
    );

    public void invalidateChallenge(
            UUID challengeId,
            OffsetDateTime now
    );

}
