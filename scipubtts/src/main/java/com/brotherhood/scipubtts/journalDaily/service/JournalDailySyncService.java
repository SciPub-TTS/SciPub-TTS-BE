package com.brotherhood.scipubtts.journalDaily.service;

import java.time.LocalDate;

public interface JournalDailySyncService {

    int syncNewArticles();

    int syncArticles(LocalDate fromDate, LocalDate toDate);
}
