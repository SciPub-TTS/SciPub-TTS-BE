package com.brotherhood.scipubtts.journalDaily.service;

import com.brotherhood.scipubtts.journalDaily.dto.response.JournalDailyArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JournalDailyArticleService {

    Page<JournalDailyArticleResponse> searchByTitle(
            String keyword,
            Pageable pageable
    );

}
