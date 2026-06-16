package com.brotherhood.scipubtts.feed.client;


import com.brotherhood.scipubtts.feed.dto.response.OpenAlexWorksResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAlexWorksClient {

    private final RestClient openAlexRestClient;

    public OpenAlexWorksResponse fetchWorksPage(
            String targetType,
            String targetOpenalexId,
            LocalDate fromDate,
            LocalDate toDate,
            String cursor
    ) {
        String filter = buildFilter(targetType, normalizeOpenAlexId(targetOpenalexId), fromDate, toDate);
        return fetchWithRetry(filter, cursor, 1);
    }

    private OpenAlexWorksResponse fetchWithRetry(String filter, String cursor, int attempt) {
        try {
            return openAlexRestClient.get() // Sử dụng trực tiếp từ config truyền vào
                    .uri(uriBuilder -> uriBuilder
                            .path("/works")
                            .queryParam("filter", filter)
                            .queryParam("sort", "publication_date:desc")
                            .queryParam("per_page", 100)
                            .queryParam("cursor", cursor)
                            .queryParam("select", "id,display_name,publication_year,publication_date,cited_by_count,authorships,primary_location")
                            .build())
                    .retrieve()
                    .body(OpenAlexWorksResponse.class);

        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();

            if ((status == 429 || status >= 500) && attempt < 3) {
                sleepBackoff(attempt);
                return fetchWithRetry(filter, cursor, attempt + 1);
            }
            throw ex;
        }
    }

    private String buildFilter(String targetType, String targetOpenalexId, LocalDate fromDate, LocalDate toDate) {
        String targetFilter = switch (targetType) {
            case "TOPIC" -> "topics.id:" + targetOpenalexId;
            case "AUTHOR" -> "authorships.author.id:" + targetOpenalexId;
            default -> throw new IllegalArgumentException("Unsupported feed target type: " + targetType);
        };

        return targetFilter
                + ",from_publication_date:" + fromDate
                + ",to_publication_date:" + toDate;
    }

    private String normalizeOpenAlexId(String openalexId) {
        if (!StringUtils.hasText(openalexId)) {
            return openalexId;
        }

        int lastSlash = openalexId.lastIndexOf("/");
        if (lastSlash >= 0) {
            return openalexId.substring(lastSlash + 1);
        }

        return openalexId;
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(Duration.ofSeconds(attempt * 2L).toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenAlex retry interrupted", ex);
        }
    }
}