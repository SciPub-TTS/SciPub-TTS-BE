package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.entity.PasswordResetChallenge;
import com.brotherhood.scipubtts.auth.repository.PasswordResetChallengeRepository;
import com.brotherhood.scipubtts.auth.service.PasswordResetAttemptService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PasswordResetAttemptServiceImpl implements PasswordResetAttemptService {
    private final PasswordResetChallengeRepository challengeRepository;

    public PasswordResetAttemptServiceImpl(
            PasswordResetChallengeRepository challengeRepository
    ) {
        this.challengeRepository = challengeRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FailedAttemptResult persistFailedAttempt(UUID challengeId,
                                                    OffsetDateTime now) {
        PasswordResetChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID));

        int nextAttemptCount = challenge.getAttemptCount() + 1;
        challenge.setAttemptCount(nextAttemptCount);

        boolean attemptsExceeded = nextAttemptCount >= challenge.getMaxAttempts();

        if (attemptsExceeded) {
            challenge.setInvalidatedAt(now);
        }

        challengeRepository.save(challenge);

        return new FailedAttemptResult(nextAttemptCount, attemptsExceeded);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void invalidateChallenge(UUID challengeId, OffsetDateTime now) {
        PasswordResetChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID));

        if (challenge.getInvalidatedAt() == null) {
            challenge.setInvalidatedAt(now);
            challengeRepository.save(challenge);
        }
    }

    public record FailedAttemptResult(
            int attemptCount,
            boolean attemptsExceeded
    ) {
    }
}
