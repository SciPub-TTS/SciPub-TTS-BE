package com.brotherhood.scipubtts.email.service.impl;

import com.brotherhood.scipubtts.auth.dto.request.PasswordResetCodeRequestedEvent;
import com.brotherhood.scipubtts.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PasswordRecoveryMailListener {

    private final EmailService emailService;

    @Async("mailExecutor")
    // Function: A smart safeguard to synchronize the database with the email service.
    // Behavior: Guarantees that the email is sent ONLY AFTER the transaction saving the code has successfully COMMITTED.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCodeRequested(PasswordResetCodeRequestedEvent event) {
        emailService.sendPasswordResetCode(event.email(), event.code());
    }
}
