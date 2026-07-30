package com.brotherhood.scipubtts.journalDaily.controller;

import com.brotherhood.scipubtts.journalDaily.service.JournalDailySyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
public class JournalDailyController {

    private final JournalDailySyncService syncService;

    // Chạy tạm 1 lần, chạy xong xóa luôn API này cũng được
    @GetMapping("/api/setup/seed-30-days")
    public String seedData() {
        LocalDate toDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate fromDate = toDate.minusDays(150);

        int saved = syncService.syncArticles(fromDate, toDate);

        return "Xong phim! Đã lưu được " + saved + " bài báo.";
    }
}
