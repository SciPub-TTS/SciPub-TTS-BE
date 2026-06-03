package com.brotherhood.scipubtts.schedule;

import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class VerificationCleanupScheduler {

    private final UserRepository userRepository;

    public VerificationCleanupScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredUnverifiedUsers() {
        // Tìm các user chưa verify và được tạo trước đó 48 tiếng
        // (Lưu ý: Bạn cần thêm trường `createdAt` (OffsetDateTime) vào Entity User để làm việc này)
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(2);

        List<User> trashUsers = userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(threshold);

        if (!trashUsers.isEmpty()) {
            userRepository.deleteAll(trashUsers);
        }
    }

}
