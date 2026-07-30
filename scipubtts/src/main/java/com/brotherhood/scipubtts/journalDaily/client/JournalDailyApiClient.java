package com.brotherhood.scipubtts.journalDaily.client;

import com.brotherhood.scipubtts.journalDaily.dto.request.JournalDailyResultItem;
import com.brotherhood.scipubtts.journalDaily.dto.response.JournalDailyApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JournalDailyApiClient {

    private static final String GUARDIAN_BASE_URL = "https://content.guardianapis.com";

    private static final String SEARCH_QUERY =
            "(\"research paper\" OR \"scientific study\" OR \"journal\" " +
                    "OR \"computer science\" OR \"software engineering\" OR \"academic research\") " +
                    "AND NOT (politics OR football OR economy OR business OR finance " +
                    "OR entertainment OR celebrity OR fashion OR psychology OR art OR arts " +
                    "OR culture OR \"mental health\" OR books OR music)";

    @Value("${guardian.api.key}")
    private String apiKey;

    @Value("${guardian.api.page-size:10}")
    private int pageSize;

    private final RestClient restClient;

    public JournalDailyApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl(GUARDIAN_BASE_URL)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public List<JournalDailyResultItem> fetchLatestArticles(LocalDate fromDate, LocalDate toDate) {
        String fromDateStr = (fromDate != null) ? fromDate.toString() : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        String toDateStr = (toDate != null) ? toDate.toString() : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();

        List<JournalDailyResultItem> allArticles = new ArrayList<>();
        int currentPage = 1;
        int totalPages = 1;
        int pageSize = 50; // Lấy tối đa 50 bài/trang (Guardian cho phép max 50-100)

        log.info("[Guardian] Starting to fetch ALL articles between {} and {}", fromDateStr, toDateStr);

        do {
            final int pageToFetch = currentPage; // Biến tạm cho lambda uriBuilder
            try {
                JournalDailyApiResponse response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/search")
                                .queryParam("q",           SEARCH_QUERY)
                                .queryParam("section",     "science|technology")
                                .queryParam("from-date",   fromDateStr)
                                .queryParam("to-date",     toDateStr)
                                .queryParam("show-fields", "trailText,thumbnail,byline")
                                .queryParam("show-tags",   "keyword")
                                .queryParam("order-by",    "newest")
                                .queryParam("pillar",      "news")
                                .queryParam("type", "article")
                                .queryParam("page-size",   pageSize)
                                .queryParam("page",        pageToFetch)
                                .queryParam("api-key",     apiKey)
                                .build())
                        .retrieve()
                        .body(JournalDailyApiResponse.class);

                if (response == null || response.response() == null || response.response().results() == null) {
                    log.warn("[Guardian] Null response on page {}", pageToFetch);
                    break;
                }

                if (!"ok".equalsIgnoreCase(response.response().status())) {
                    log.warn("[Guardian] Status not ok on page {}: {}", pageToFetch, response.response().status());
                    break;
                }

                List<JournalDailyResultItem> results = response.response().results();
                if (results.isEmpty()) {
                    break;
                }

                allArticles.addAll(results);

                totalPages = response.response().pages();
                log.info("[Guardian] Fetched page {}/{} ({} items)", pageToFetch, totalPages, results.size());

                currentPage++;

                Thread.sleep(100);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[Guardian] Interrupted while fetching pages", e);
                break;
            } catch (RestClientException e) {
                log.error("[Guardian] Error on page {}: {}", pageToFetch, e.getMessage(), e);
                break;
            }
        } while (currentPage <= totalPages);

        log.info("[Guardian] Successfully fetched total {} articles across {} pages", allArticles.size(), totalPages);
        return allArticles;
    }
}