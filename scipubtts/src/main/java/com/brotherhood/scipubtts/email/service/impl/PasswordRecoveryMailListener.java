package com.brotherhood.scipubtts.email.service.impl;

import com.brotherhood.scipubtts.auth.service.PasswordResetCodeRequestedEvent;
import com.brotherhood.scipubtts.auth.service.PasswordResetCompletedEvent;
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
    //Chức năng: Đây là chốt chặn cực kỳ thông minh nhằm đồng bộ hóa giữa Database và Email.
    //Hoạt động: Nó ra lệnh: "Chỉ được phép chạy hàm gửi email này SAU KHI giao dịch lưu mã code vào Database đã COMMIT (thành công hoàn toàn)".
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCodeRequested(PasswordResetCodeRequestedEvent event) {
        emailService.sendPasswordResetCode(event.email(), event.code());
    }

//    @Async("mailExecutor")
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void onPasswordResetCompleted(PasswordResetCompletedEvent event) {
//        emailService.sendPasswordResetSuccessNotice(event.email());
//    }
}
