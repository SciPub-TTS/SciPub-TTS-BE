package com.brotherhood.scipubtts.email.listener;

import com.brotherhood.scipubtts.email.event.UserBannedEvent;
import com.brotherhood.scipubtts.email.event.UserUnbannedEvent;
import com.brotherhood.scipubtts.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserAccountStatusMailListener {

    private final EmailService emailService;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserBanned(UserBannedEvent event) {
        emailService.sendAccountBannedEmail(event.email());
    }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserUnbanned(UserUnbannedEvent event) {
        emailService.sendAccountUnbannedEmail(event.email());
    }
}