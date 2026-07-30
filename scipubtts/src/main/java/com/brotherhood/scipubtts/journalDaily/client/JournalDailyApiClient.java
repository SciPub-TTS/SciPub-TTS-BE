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

//    @Value("${guardian.api.page-size:10}")
//    private int pageSize;

    private final RestClient restClient;

    public JournalDailyApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl(GUARDIAN_BASE_URL)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public List<JournalDailyResultItem> fetchLatestArticles() {
        // Get today's date in YYYY-MM-DD format based on the Asia/Ho_Chi_Minh time zone
        String todayStr = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        log.info("[Guardian] Fetching articles published on date: {}", todayStr);

        try {
            JournalDailyApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q",            SEARCH_QUERY)
                            .queryParam("section",      "science|technology")
                            .queryParam("from-date",    todayStr) // Only retrieve articles starting FROM today
                            .queryParam("to-date",      todayStr) // Up to the end of today
                            .queryParam("show-fields",  "trailText,thumbnail,byline")
                            .queryParam("show-tags",    "keyword")
                            .queryParam("order-by",     "newest")
//                            .queryParam("page-size",    pageSize)
                            .queryParam("pillar",       "news")
                            .queryParam("api-key",      apiKey)
                            .build())
                    .retrieve()
                    .body(JournalDailyApiResponse.class);

            if (response == null
                    || response.response() == null
                    || response.response().results() == null) {
                log.warn("[Guardian] API returned null or empty response for date {}", todayStr);
                return List.of();
            }

            if (!"ok".equalsIgnoreCase(response.response().status())) {
                log.warn("[Guardian] API status is not 'ok': {}", response.response().status());
                return List.of();
            }

            log.info("[Guardian] Successfully fetched {} articles for date {}",
                    response.response().results().size(), todayStr);
            return response.response().results();

        } catch (RestClientException e) {
            log.error("[Guardian] Error occurred while calling API for date {}: {}", todayStr, e.getMessage(), e);
            return List.of();
        }
    }

    public List<JournalDailyResultItem> fetchLatestArticles(LocalDate fromDate, LocalDate toDate) {
        // Chuyển LocalDate sang chuỗi YYYY-MM-DD
        String fromDateStr = (fromDate != null) ? fromDate.toString() : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        String toDateStr = (toDate != null) ? toDate.toString() : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();

        log.info("[Guardian] Fetching articles between {} and {}", fromDateStr, toDateStr);

        try {
            JournalDailyApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q",           SEARCH_QUERY)
                            .queryParam("section",     "science|technology")
                            .queryParam("from-date",   fromDateStr) // Định dạng YYYY-MM-DD
                            .queryParam("to-date",     toDateStr)   // Định dạng YYYY-MM-DD
                            .queryParam("show-fields", "trailText,thumbnail,byline")
                            .queryParam("show-tags",   "keyword")
                            .queryParam("order-by",    "newest")
                            .queryParam("pillar",      "news")
                            .queryParam("api-key",     apiKey)
                            .build())
                    .retrieve()
                    .body(JournalDailyApiResponse.class);

            if (response == null
                    || response.response() == null
                    || response.response().results() == null) {
                log.warn("[Guardian] API returned null or empty response for range {} to {}", fromDateStr, toDateStr);
                return List.of();
            }

            if (!"ok".equalsIgnoreCase(response.response().status())) {
                log.warn("[Guardian] API status is not 'ok': {}", response.response().status());
                return List.of();
            }

            List<JournalDailyResultItem> results = response.response().results();
            log.info("[Guardian] Successfully fetched {} articles between {} and {}",
                    results.size(), fromDateStr, toDateStr);
            return results;

        } catch (RestClientException e) {
            log.error("[Guardian] Error occurred while calling API for range {} to {}: {}",
                    fromDateStr, toDateStr, e.getMessage(), e);
            return List.of();
        }
    }
}