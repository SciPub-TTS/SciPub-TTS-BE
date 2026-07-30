package com.brotherhood.scipubtts.schedule;

import com.brotherhood.scipubtts.journalDaily.service.JournalDailySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JournalDailySyncScheduler {
    private final JournalDailySyncService guardianSyncService;

    /**
     * Runs daily at 02:00:00 Vietnam time.
     * zone = "Asia/Ho_Chi_Minh" lets Spring automatically handle the UTC offset,
     * eliminating the need to hardcode 19:00 UTC.
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void scheduleDailySync() {
        log.info("[Guardian Scheduler] === Starting Guardian API sync CronJob ===");
        try {
            int saved = guardianSyncService.syncNewArticles();
            log.info("[Guardian Scheduler] === Completed: saved {} new articles ===", saved);
        } catch (Exception e) {
            // Log the error without rethrowing — allows the CronJob to run normally next time
            log.error("[Guardian Scheduler] === CronJob error: {} ===", e.getMessage(), e);
        }
    }
}