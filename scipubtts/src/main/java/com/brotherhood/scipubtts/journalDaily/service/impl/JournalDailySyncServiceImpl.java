package com.brotherhood.scipubtts.journalDaily.service.impl;

import com.brotherhood.scipubtts.feed.entity.ApiJob;
import com.brotherhood.scipubtts.feed.repository.ApiJobRepository;
import com.brotherhood.scipubtts.journalDaily.client.JournalDailyApiClient;
import com.brotherhood.scipubtts.journalDaily.dto.request.JournalDailyResultItem;
import com.brotherhood.scipubtts.journalDaily.entity.JournalDailyArticle;
import com.brotherhood.scipubtts.journalDaily.repository.JournalDailyArticleRepository;
import com.brotherhood.scipubtts.journalDaily.service.JournalDailySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalDailySyncServiceImpl implements JournalDailySyncService {

    private static final String JOB_TYPE = "JOURNAL_DAILY_SYNC";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_PARTIAL_SUCCESS = "PARTIAL_SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final JournalDailyApiClient journalDailyApiClient;
    private final JournalDailyArticleMapper mapper;
    private final JournalDailyArticleRepository articleRepository;
    private final ApiJobRepository apiJobRepository; // Inject Repository for Job management

    /**
     * Fetches articles from the JournalDaily API, filters duplicates, and saves new articles.
     * Updates the execution progress in the api_job table.
     */
    @Override
    @Transactional
    public int syncNewArticles() {
        LocalDate toDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate fromDate = toDate.minusDays(1);

        log.info("[JournalDaily Sync] Triggering daily sync for range {} to {}", fromDate, toDate);
        return syncArticles(fromDate, toDate);
    }

    @Override
    @Transactional
    public int syncArticles(LocalDate fromDate, LocalDate toDate) {
        log.info("[JournalDaily Sync] Starting article synchronization between {} and {}", fromDate, toDate);

        // 1. Initialize and save RUNNING status for the Job
        ApiJob job = ApiJob.builder()
                .jobType(JOB_TYPE)
                .status(STATUS_RUNNING)
                .startedAt(OffsetDateTime.now())
                .totalFetched(0)
                .totalSaved(0)
                .totalFailed(0)
                .build();
        ApiJob savedJob = apiJobRepository.save(job);

        try {
            // API client will fetch all articles page-by-page for this date range
            List<JournalDailyResultItem> items = Optional.ofNullable(
                    journalDailyApiClient.fetchLatestArticles(fromDate, toDate)
            ).orElseGet(List::of);

            if (items.isEmpty()) {
                log.info("[JournalDaily Sync] API returned no articles for range {} to {}", fromDate, toDate);
                completeJob(savedJob, 0, 0, 0, STATUS_SUCCESS);
                return 0;
            }

            // Map DTO to Entity, filter out non-articles and invalid records
            List<JournalDailyArticle> mappedArticles = items.stream()
                    .filter(item -> "article".equalsIgnoreCase(item.type()))
                    .map(mapper::toEntity)
                    .filter(Objects::nonNull)
                    .filter(article -> article.getExternalId() != null)
                    .toList();

            int invalidCount = items.size() - mappedArticles.size();

            if (mappedArticles.isEmpty()) {
                log.warn("[JournalDaily Sync] All {} items are invalid after mapping", items.size());
                completeJob(savedJob, items.size(), 0, invalidCount, STATUS_PARTIAL_SUCCESS);
                return 0;
            }

            // Remove duplicates within the API response itself
            Map<String, JournalDailyArticle> uniqueArticlesMap = mappedArticles.stream()
                    .collect(Collectors.toMap(
                            JournalDailyArticle::getExternalId,
                            Function.identity(),
                            (first, duplicate) -> first,
                            LinkedHashMap::new
                    ));

            List<JournalDailyArticle> uniqueArticles = new ArrayList<>(uniqueArticlesMap.values());
            int responseDuplicateCount = mappedArticles.size() - uniqueArticles.size();

            // Batch check for existing externalIds in the DB
            List<String> externalIds = uniqueArticles.stream()
                    .map(JournalDailyArticle::getExternalId)
                    .toList();
            Set<String> existingIds = articleRepository.findExistingExternalIds(externalIds);

            List<JournalDailyArticle> newArticles = uniqueArticles.stream()
                    .filter(article -> !existingIds.contains(article.getExternalId()))
                    .toList();

            if (newArticles.isEmpty()) {
                log.info(
                        "[JournalDaily Sync] No new articles found. API={}, invalid={}, responseDuplicate={}, existing={}",
                        items.size(), invalidCount, responseDuplicateCount, existingIds.size()
                );
                completeJob(savedJob, items.size(), 0, invalidCount, STATUS_SUCCESS);
                return 0;
            }

            // Save new articles
            articleRepository.saveAll(newArticles);

            log.info(
                    "[JournalDaily Sync] Synchronization completed. API={}, invalid={}, responseDuplicate={}, existing={}, saved={}",
                    items.size(), invalidCount, responseDuplicateCount, existingIds.size(), newArticles.size()
            );

            // Determine final status (If any articles failed mapping -> PARTIAL_SUCCESS)
            String finalStatus = (invalidCount > 0) ? STATUS_PARTIAL_SUCCESS : STATUS_SUCCESS;
            completeJob(savedJob, items.size(), newArticles.size(), invalidCount, finalStatus);

            return newArticles.size();

        } catch (Exception ex) {
            log.error("[JournalDaily Sync] Unexpected error occurred during synchronization", ex);
            failJob(savedJob, ex);
            throw ex; // Rethrow to trigger Transaction rollback if necessary
        }
    }

    /**
     * Helper method to update Job on success
     */
    private void completeJob(ApiJob job, int fetched, int saved, int failed, String status) {
        job.setTotalFetched(fetched);
        job.setTotalSaved(saved);
        job.setTotalFailed(failed); // Number of formatting errors (filtered out by mapper)
        job.setStatus(status);
        job.setFinishedAt(OffsetDateTime.now());
        apiJobRepository.save(job);
    }

    /**
     * Helper method to update Job on Exception
     */
    private void failJob(ApiJob job, Exception ex) {
        job.setStatus(STATUS_FAILED);
        job.setFinishedAt(OffsetDateTime.now());

        // Prevent String overflow if Exception message is too long
        String errorMsg = ex.getMessage();
        if (errorMsg != null && errorMsg.length() > 2000) {
            errorMsg = errorMsg.substring(0, 2000) + "...";
        }
        job.setErrorLog(errorMsg);

        // Note: Use a requireNew transaction or catch independently outside the caller
        // if you want the DB to save the error log even if this method's transaction rolls back.
        apiJobRepository.save(job);
    }
}