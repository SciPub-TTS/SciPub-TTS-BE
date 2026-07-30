package com.brotherhood.scipubtts.journalDaily.controller;

import com.brotherhood.scipubtts.journalDaily.dto.response.JournalDailyArticleResponse;
import com.brotherhood.scipubtts.journalDaily.service.JournalDailyArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journal-daily/articles")
@RequiredArgsConstructor
public class JournalDailyArticleController {

    private static final int MAX_PAGE_SIZE = 100;

    private final JournalDailyArticleService articleService;

    @GetMapping("/search")
    public ResponseEntity<Page<JournalDailyArticleResponse>> searchByTitle(
            @RequestParam(
                    name = "title",
                    required = false,
                    defaultValue = ""
            )
            String title,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "12"
            )
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Order.desc("publishedAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<JournalDailyArticleResponse> result =
                articleService.searchByTitle(title, pageable);

        return ResponseEntity.ok(result);
    }
}